package maeilbatch.smtp;

import jakarta.mail.Transport;

@FunctionalInterface
public interface SmtpConnectionCallback {

    void execute(Transport transport) throws Exception;
}
