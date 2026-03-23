package maeilbatch;

import jakarta.mail.Session;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public record SmtpConnectionProperties(
        Session session,
        String host,
        int port,
        String protocol,
        String username,
        String password
) {

    public static SmtpConnectionProperties from(JavaMailSenderImpl delegate) {
        Session session = delegate.getSession();
        String protocol = delegate.getProtocol() != null
                ? delegate.getProtocol()
                : JavaMailSenderImpl.DEFAULT_PROTOCOL;

        return new SmtpConnectionProperties(
                session,
                delegate.getHost(),
                delegate.getPort(),
                protocol,
                emptyToNull(delegate.getUsername()),
                emptyToNull(delegate.getPassword())
        );
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
