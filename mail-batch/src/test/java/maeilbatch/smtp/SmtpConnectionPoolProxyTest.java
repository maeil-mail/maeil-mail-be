package maeilbatch.smtp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.Address;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

class SmtpConnectionPoolProxyTest {

    @Test
    @DisplayName("SimpleMailMessage 전송은 지원하지 않는다.")
    void sendSimpleMailMessageUnsupported() {
        SmtpConnectionPoolProxy proxy = new SmtpConnectionPoolProxy(
                mock(JavaMailSenderImpl.class),
                mock(SmtpConnectionPool.class)
        );

        assertThatThrownBy(() -> proxy.send(new SimpleMailMessage()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("MimeMessage 전송 시 커넥션 풀을 통해 하나의 Transport로 메시지를 전송한다.")
    void sendMimeMessagesWithPooledConnection() throws Exception {
        JavaMailSenderImpl delegate = mock(JavaMailSenderImpl.class);
        SmtpConnectionPool connectionPool = mock(SmtpConnectionPool.class);
        Transport transport = mock(Transport.class);
        MimeMessage first = mock(MimeMessage.class);
        MimeMessage second = mock(MimeMessage.class);
        Address[] recipients = {new InternetAddress("team@maeilmail.com")};

        when(first.getSentDate()).thenReturn(null);
        when(second.getSentDate()).thenReturn(null);
        when(first.getMessageID()).thenReturn("<first@maeilmail>");
        when(second.getMessageID()).thenReturn("<second@maeilmail>");
        when(first.getAllRecipients()).thenReturn(recipients);
        when(second.getAllRecipients()).thenReturn(recipients);
        doAnswer(invocation -> {
            SmtpConnectionCallback callback = invocation.getArgument(0);
            callback.execute(transport);
            return null;
        }).when(connectionPool).doWithConnection(any(SmtpConnectionCallback.class));

        SmtpConnectionPoolProxy proxy = new SmtpConnectionPoolProxy(delegate, connectionPool);

        proxy.send(first, second);

        assertAll(
                () -> verify(connectionPool, times(1)).doWithConnection(any(SmtpConnectionCallback.class)),
                () -> verify(transport, times(1)).sendMessage(first, recipients),
                () -> verify(transport, times(1)).sendMessage(second, recipients),
                () -> verify(first, times(1)).saveChanges(),
                () -> verify(second, times(1)).saveChanges(),
                () -> verify(first, times(1)).setHeader("Message-ID", "<first@maeilmail>"),
                () -> verify(second, times(1)).setHeader("Message-ID", "<second@maeilmail>")
        );
    }

    @Test
    @DisplayName("커넥션 풀 실행 중 예외가 나면 MailSendException으로 감싼다.")
    void sendMimeMessagesWrapsException() throws Exception {
        JavaMailSenderImpl delegate = mock(JavaMailSenderImpl.class);
        SmtpConnectionPool connectionPool = mock(SmtpConnectionPool.class);
        MimeMessage mimeMessage = mock(MimeMessage.class);

        doThrow(new IllegalStateException("borrow fail"))
                .when(connectionPool)
                .doWithConnection(any(SmtpConnectionCallback.class));

        SmtpConnectionPoolProxy proxy = new SmtpConnectionPoolProxy(delegate, connectionPool);

        assertThatThrownBy(() -> proxy.send(mimeMessage))
                .isInstanceOf(MailSendException.class)
                .hasMessage("메일 전송을 실패했습니다.")
                .hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("testConnection은 연결된 transport면 통과한다.")
    void testConnectionSuccess() throws Exception {
        JavaMailSenderImpl delegate = mock(JavaMailSenderImpl.class);
        SmtpConnectionPool connectionPool = mock(SmtpConnectionPool.class);
        Transport transport = mock(Transport.class);

        when(transport.isConnected()).thenReturn(true);
        doAnswer(invocation -> {
            SmtpConnectionCallback callback = invocation.getArgument(0);
            callback.execute(transport);
            return null;
        }).when(connectionPool).doWithConnection(any(SmtpConnectionCallback.class));

        SmtpConnectionPoolProxy proxy = new SmtpConnectionPoolProxy(delegate, connectionPool);

        proxy.testConnection();

        verify(connectionPool, times(1)).doWithConnection(any(SmtpConnectionCallback.class));
        verify(transport, times(1)).isConnected();
    }

    @Test
    @DisplayName("testConnection은 연결 검증이 실패하면 IllegalStateException을 던진다.")
    void testConnectionFailureWhenTransportDisconnected() throws Exception {
        JavaMailSenderImpl delegate = mock(JavaMailSenderImpl.class);
        SmtpConnectionPool connectionPool = mock(SmtpConnectionPool.class);
        Transport transport = mock(Transport.class);

        when(transport.isConnected()).thenReturn(false);
        doAnswer(invocation -> {
            SmtpConnectionCallback callback = invocation.getArgument(0);
            callback.execute(transport);
            return null;
        }).when(connectionPool).doWithConnection(any(SmtpConnectionCallback.class));

        SmtpConnectionPoolProxy proxy = new SmtpConnectionPoolProxy(delegate, connectionPool);

        assertThatThrownBy(proxy::testConnection)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("테스트 SMTP 연결을 실패했습니다.");
    }

    @Test
    @DisplayName("createMimeMessage는 delegate에 위임한다.")
    void createMimeMessageDelegates() {
        JavaMailSenderImpl delegate = mock(JavaMailSenderImpl.class);
        SmtpConnectionPool connectionPool = mock(SmtpConnectionPool.class);
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(delegate.createMimeMessage()).thenReturn(mimeMessage);

        SmtpConnectionPoolProxy proxy = new SmtpConnectionPoolProxy(delegate, connectionPool);

        MimeMessage result = proxy.createMimeMessage();

        assertThat(result).isSameAs(mimeMessage);
    }
}
