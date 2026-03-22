package maeilbatch;

import java.io.InputStream;
import java.util.Date;
import jakarta.mail.Address;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public class ConnectionPoolJavaMailSender implements JavaMailSender, AutoCloseable {

    private final JavaMailSenderImpl delegate;
    private final GenericObjectPool<Transport> connectionPool;

    public ConnectionPoolJavaMailSender(JavaMailSenderImpl delegate) {
        GenericObjectPoolConfig<Transport> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(8);
        config.setMaxIdle(8);
        config.setMinIdle(0);
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(true);
        config.setBlockWhenExhausted(true);

        this.delegate = delegate;
        this.connectionPool = new GenericObjectPool<>(new ConnectionPooledObjectFactory(delegate), config);
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
    }

    @Override
    public void close() {
        connectionPool.close();
    }

    @Override
    public void send(MimeMessage... mimeMessages) throws MailException {
        Transport transport = getConnection();
        boolean invalidateTrarget = false;

        for (MimeMessage mimeMessage : mimeMessages) {
            try {
                sendMessage(transport, mimeMessage);
            } catch (Exception e) {
                invalidateTrarget = true;
                throw new MailSendException("메일 전송을 실패했습니다.", e);
            } finally {
                returnOrInvalidate(transport, invalidateTrarget);
            }
        }
    }

    private Transport getConnection()  {
        try {
            return connectionPool.borrowObject();
        } catch (Exception e) {
            throw new MailSendException("SMTP 커넥션 획득에 실패했습니다.", e);
        }
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

    private void returnOrInvalidate(Transport transport, boolean invalidateTransport) {
        if (transport == null) {
            return;
        }

        try {
            if (invalidateTransport) {
                connectionPool.invalidateObject(transport);
                return;
            }
            connectionPool.returnObject(transport);
        } catch (Exception e) {
            throw new MailSendException("SMTP 커넥션 반납에 실패했습니다.", e);
        }
    }
}
