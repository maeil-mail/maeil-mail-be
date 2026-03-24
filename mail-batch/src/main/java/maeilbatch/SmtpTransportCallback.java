package maeilbatch;

import jakarta.mail.Transport;

@FunctionalInterface
public interface SmtpTransportCallback {

    void execute(Transport transport) throws Exception;
}
