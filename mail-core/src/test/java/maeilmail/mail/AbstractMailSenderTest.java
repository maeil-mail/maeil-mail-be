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

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
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
        MimeMessage thirdMimeMessage = mock(MimeMessage.class);
        SimpleMailMessage message = new SimpleMailMessage("to@test.com", "subject", "text", "type");

        when(javaMailSender.createMimeMessage()).thenReturn(firstMimeMessage, secondMimeMessage, thirdMimeMessage);
        when(mimeMessageCustomizer.customize(firstMimeMessage, message)).thenReturn(firstMimeMessage);
        when(mimeMessageCustomizer.customize(secondMimeMessage, message)).thenReturn(secondMimeMessage);
        when(mimeMessageCustomizer.customize(thirdMimeMessage, message)).thenReturn(thirdMimeMessage);
        doThrow(new MailSendException("temporary send error"))
                .when(javaMailSender)
                .send(any(MimeMessage.class));

        assertThatCode(() -> testMailSender.sendMailSync(message))
                .doesNotThrowAnyException();

        assertAll(
                () -> verify(limiter, times(3)).consumeBlocking(WAIT_TIMEOUT),
                () -> verify(javaMailSender, times(3)).createMimeMessage(),
                () -> verify(javaMailSender, times(1)).send(firstMimeMessage),
                () -> verify(javaMailSender, times(1)).send(secondMimeMessage),
                () -> verify(javaMailSender, times(1)).send(thirdMimeMessage),
                () -> assertThat(testMailSender.successCount()).isZero(),
                () -> assertThat(testMailSender.failureCount()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("재시도 대상이 아닌 예외는 즉시 실패 처리한다.")
    void doesNotRetryForNonRetryableException() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        SimpleMailMessage message = new SimpleMailMessage("to@test.com", "subject", "text", "type");

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(mimeMessageCustomizer.customize(mimeMessage, message))
                .thenThrow(new IllegalArgumentException("invalid message"));

        testMailSender.sendMailSync(message);

        assertAll(
                () -> verify(limiter, times(1)).consumeBlocking(WAIT_TIMEOUT),
                () -> verify(javaMailSender, times(1)).createMimeMessage(),
                () -> verify(javaMailSender, times(0)).send(mimeMessage),
                () -> assertThat(testMailSender.successCount()).isZero(),
                () -> assertThat(testMailSender.failureCount()).isEqualTo(1)
        );
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

        int successCount() {
            return successCount.get();
        }

        int failureCount() {
            return failureCount.get();
        }

        void reset() {
            successCount.set(0);
            failureCount.set(0);
        }
    }
}
