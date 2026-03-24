package maeilbatch;

import java.io.InputStream;
import java.util.Date;
import jakarta.mail.Address;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public class SmtpConnectionPoolProxy implements JavaMailSender, AutoCloseable {

    private final JavaMailSenderImpl delegate;
    private final SmtpConnectionPool connectionPool;

    public SmtpConnectionPoolProxy(
            JavaMailSenderImpl delegate,
            SmtpConnectionPool connectionPool
    ) {
        this.delegate = delegate;
        this.connectionPool = connectionPool;
    }

    @Override
    public void close() {
        connectionPool.close();
    }

    @Override
    public MimeMessage createMimeMessage() {
        return delegate.createMimeMessage();
    }

    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        return delegate.createMimeMessage(contentStream);
    }

    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(MimeMessage... mimeMessages) throws MailException {
        try {
            connectionPool.doWithConnection(getSendMailCallBack(mimeMessages));
        } catch (Exception e) {
            throw new MailSendException("메일 전송을 실패했습니다.", e);
        }
    }

    private SmtpConnectionPool.TransportCallback getSendMailCallBack(MimeMessage[] mimeMessages) {
        return transport -> {
            for (MimeMessage mimeMessage : mimeMessages) {
                sendMessage(transport, mimeMessage);
            }
        };
    }

    private void sendMessage(Transport transport, MimeMessage mimeMessage) throws Exception {
        if (mimeMessage.getSentDate() == null) {
            mimeMessage.setSentDate(new Date());
        }
        String messageId = mimeMessage.getMessageID();
        mimeMessage.saveChanges();
        if (messageId != null) {
            mimeMessage.setHeader("Message-ID", messageId);
        }

        Address[] recipients = mimeMessage.getAllRecipients();
        transport.sendMessage(mimeMessage, recipients != null ? recipients : new Address[0]);
    }

    public void testConnection() {
        String message = "테스트 SMTP 연결을 실패했습니다.";

        try {
            connectionPool.doWithConnection(transport -> {
                if (!transport.isConnected()) {
                    throw new IllegalStateException(message);
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException(message, e);
        }
    }
}
