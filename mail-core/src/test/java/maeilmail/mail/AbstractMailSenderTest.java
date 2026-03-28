package maeilmail.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import maeilmail.support.DistributedRateLimitSupport;
import maeilmail.support.IntegrationTestSupport;
import maeilmail.support.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.retry.annotation.EnableRetry;

@Import(AbstractMailSenderTest.TestConfig.class)
class AbstractMailSenderTest extends IntegrationTestSupport {

    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(2);

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private MimeMessageCustomizer mimeMessageCustomizer;

    @Autowired
    private DistributedRateLimitSupport limiter;

    @Autowired
    private TestMailSender testMailSender;

    @BeforeEach
    void setUpAbstractMailSender() {
        reset(javaMailSender, mimeMessageCustomizer, limiter);
        testMailSender.reset();
        when(limiter.consumeBlocking(WAIT_TIMEOUT)).thenReturn(true);
    }

    @Test
    @DisplayName("MailException이 한번 발생하면 재시도 후 성공 처리한다.")
    void retriesWhenMailExceptionOccurs() throws Exception {
        MimeMessage firstMimeMessage = mock(MimeMessage.class);
        MimeMessage secondMimeMessage = mock(MimeMessage.class);
        SimpleMailMessage message = new SimpleMailMessage("to@test.com", "subject", "text", "type");

        when(javaMailSender.createMimeMessage()).thenReturn(firstMimeMessage, secondMimeMessage);
        when(mimeMessageCustomizer.customize(firstMimeMessage, message)).thenReturn(firstMimeMessage);
        when(mimeMessageCustomizer.customize(secondMimeMessage, message)).thenReturn(secondMimeMessage);
        doThrow(new MailSendException("temporary send error"))
                .doNothing()
                .when(javaMailSender)
                .send(firstMimeMessage);

        testMailSender.sendMailSync(message);

        assertAll(
                () -> verify(limiter, times(2)).consumeBlocking(WAIT_TIMEOUT),
                () -> verify(javaMailSender, times(2)).createMimeMessage(),
                () -> verify(javaMailSender, times(1)).send(firstMimeMessage),
                () -> verify(javaMailSender, times(1)).send(secondMimeMessage),
                () -> assertThat(testMailSender.successCount()).isEqualTo(1),
                () -> assertThat(testMailSender.failureCount()).isZero()
        );
    }

    @Test
    @DisplayName("MessagingException이 발생하면 즉시 실패 처리한다.")
    void handlesMessagingExceptionAsFailure() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        SimpleMailMessage message = new SimpleMailMessage("to@test.com", "subject", "text", "type");

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(mimeMessageCustomizer.customize(mimeMessage, message))
                .thenThrow(new MessagingException("temporary mime error"));

        testMailSender.sendMailSync(message);

        assertAll(
                () -> verify(limiter, times(1)).consumeBlocking(WAIT_TIMEOUT),
                () -> verify(javaMailSender, times(1)).createMimeMessage(),
                () -> verify(javaMailSender, times(0)).send(mimeMessage),
                () -> assertThat(testMailSender.successCount()).isZero(),
                () -> assertThat(testMailSender.failureCount()).isEqualTo(1),
                () -> assertThat(testMailSender.ambiguousCount()).isZero()
        );
    }

    @Test
    @DisplayName("처리율 제한 예외가 한번 발생하면 재시도 후 성공 처리한다.")
    void retriesWhenRateLimitExceededOccurs() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        SimpleMailMessage message = new SimpleMailMessage("to@test.com", "subject", "text", "type");

        when(limiter.consumeBlocking(WAIT_TIMEOUT))
                .thenThrow(new RateLimitExceededException())
                .thenReturn(true);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(mimeMessageCustomizer.customize(mimeMessage, message)).thenReturn(mimeMessage);

        testMailSender.sendMailSync(message);

        assertAll(
                () -> verify(limiter, times(2)).consumeBlocking(WAIT_TIMEOUT),
                () -> verify(javaMailSender, times(1)).send(mimeMessage),
                () -> assertThat(testMailSender.successCount()).isEqualTo(1),
                () -> assertThat(testMailSender.failureCount()).isZero()
        );
    }

    @Test
    @DisplayName("재시도 가능한 예외가 계속 발생하면 최종 실패 시 한 번만 실패 처리한다.")
    void handlesFailureOnceWhenRetryIsExhausted() throws Exception {
        MimeMessage firstMimeMessage = mock(MimeMessage.class);
        MimeMessage secondMimeMessage = mock(MimeMessage.class);
        SimpleMailMessage message = new SimpleMailMessage("to@test.com", "subject", "text", "type");

        when(javaMailSender.createMimeMessage()).thenReturn(firstMimeMessage, secondMimeMessage);
        when(mimeMessageCustomizer.customize(firstMimeMessage, message)).thenReturn(firstMimeMessage);
        when(mimeMessageCustomizer.customize(secondMimeMessage, message)).thenReturn(secondMimeMessage);
        doThrow(new MailSendException("temporary send error"))
                .when(javaMailSender)
                .send(any(MimeMessage.class));

        assertThatCode(() -> testMailSender.sendMailSync(message))
                .doesNotThrowAnyException();

        assertAll(
                () -> verify(limiter, times(2)).consumeBlocking(WAIT_TIMEOUT),
                () -> verify(javaMailSender, times(2)).createMimeMessage(),
                () -> verify(javaMailSender, times(1)).send(firstMimeMessage),
                () -> verify(javaMailSender, times(1)).send(secondMimeMessage),
                () -> assertThat(testMailSender.successCount()).isZero(),
                () -> assertThat(testMailSender.failureCount()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("실제 SMTP read timeout도 ambiguous 처리한다.")
    void handlesActualSmtpReadTimeoutAsAmbiguous() throws Exception {
        try (ReadTimeoutSmtpServer smtpServer = new ReadTimeoutSmtpServer(Duration.ofSeconds(2))) {
            smtpServer.start();

            JavaMailSenderImpl realJavaMailSender = new JavaMailSenderImpl();
            realJavaMailSender.setHost("127.0.0.1");
            realJavaMailSender.setPort(smtpServer.port());

            Properties mailProperties = realJavaMailSender.getJavaMailProperties();
            mailProperties.put("mail.smtp.auth", "false");
            mailProperties.put("mail.smtp.starttls.enable", "false");
            mailProperties.put("mail.smtp.connectiontimeout", "1000");
            mailProperties.put("mail.smtp.timeout", "500");

            DistributedRateLimitSupport realLimiter = mock(DistributedRateLimitSupport.class);
            when(realLimiter.consumeBlocking(WAIT_TIMEOUT)).thenReturn(true);

            TestMailSender realTestMailSender =
                    new TestMailSender(realJavaMailSender, new MimeMessageCustomizer(), realLimiter);
            SimpleMailMessage message = new SimpleMailMessage("to@test.com", "subject", "text", "type");

            assertThatCode(() -> realTestMailSender.sendMailSync(message))
                    .doesNotThrowAnyException();

            assertAll(
                    () -> verify(realLimiter, times(1)).consumeBlocking(WAIT_TIMEOUT),
                    () -> assertThat(realTestMailSender.successCount()).isZero(),
                    () -> assertThat(realTestMailSender.failureCount()).isZero(),
                    () -> assertThat(realTestMailSender.ambiguousCount()).isEqualTo(1)
            );
        }
    }

    @EnableRetry
    @TestConfiguration
    static class TestConfig {

        @Bean
        TestMailSender testMailSender(
                JavaMailSender javaMailSender,
                MimeMessageCustomizer mimeMessageCustomizer,
                DistributedRateLimitSupport limiter
        ) {
            return new TestMailSender(javaMailSender, mimeMessageCustomizer, limiter);
        }
    }

    static class TestMailSender extends AbstractMailSender<SimpleMailMessage> {

        private final AtomicInteger successCount = new AtomicInteger();
        private final AtomicInteger failureCount = new AtomicInteger();
        private final AtomicInteger ambiguousCount = new AtomicInteger();

        TestMailSender(
                JavaMailSender javaMailSender,
                MimeMessageCustomizer mimeMessageCustomizer,
                DistributedRateLimitSupport limiter
        ) {
            super(javaMailSender, mimeMessageCustomizer, limiter);
        }

        @Override
        protected void logSending(SimpleMailMessage message) {
        }

        @Override
        protected void handleSuccess(SimpleMailMessage message) {
            successCount.incrementAndGet();
        }

        @Override
        protected void handleFailure(SimpleMailMessage message) {
            failureCount.incrementAndGet();
        }

        @Override
        protected void handleAmbiguous(SimpleMailMessage message) {
            ambiguousCount.incrementAndGet();
        }

        int successCount() {
            return successCount.get();
        }

        int failureCount() {
            return failureCount.get();
        }

        int ambiguousCount() {
            return ambiguousCount.get();
        }

        void reset() {
            successCount.set(0);
            failureCount.set(0);
            ambiguousCount.set(0);
        }
    }

    static final class ReadTimeoutSmtpServer implements AutoCloseable {

        private final ServerSocket serverSocket;
        private final long responseDelayMillis;
        private final Thread serverThread;

        ReadTimeoutSmtpServer(Duration responseDelay) throws Exception {
            this.serverSocket = new ServerSocket(0);
            this.responseDelayMillis = responseDelay.toMillis();
            this.serverThread = new Thread(this::serve, "smtp-read-timeout-server");
            this.serverThread.setDaemon(true);
        }

        void start() {
            serverThread.start();
        }

        int port() {
            return serverSocket.getLocalPort();
        }

        private void serve() {
            try (Socket socket = serverSocket.accept();
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
                 BufferedWriter writer = new BufferedWriter(
                         new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII))) {

                writeLine(writer, "220 localhost ESMTP test");

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("EHLO") || line.startsWith("HELO")) {
                        writeLine(writer, "250-localhost");
                        writeLine(writer, "250 OK");
                        continue;
                    }
                    if (line.startsWith("MAIL FROM:") || line.startsWith("RCPT TO:")) {
                        writeLine(writer, "250 OK");
                        continue;
                    }
                    if ("DATA".equals(line)) {
                        writeLine(writer, "354 End data with <CR><LF>.<CR><LF>");
                        break;
                    }
                }

                while ((line = reader.readLine()) != null) {
                    if (".".equals(line)) {
                        Thread.sleep(responseDelayMillis);
                        return;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        private void writeLine(BufferedWriter writer, String line) throws Exception {
            writer.write(line);
            writer.write("\r\n");
            writer.flush();
        }

        @Override
        public void close() throws Exception {
            serverSocket.close();
            serverThread.join(3000);
        }
    }
}
