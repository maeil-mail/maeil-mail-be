package maeilmail.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public abstract class MimeMessageCreator<T> {

    protected static final String FROM_EMAIL = "maeil-mail <noreply@maeil-mail.kr>";
    protected static final String TITLE_PREFIX = "[매일메일] %s";

    public abstract MimeMessage createMimeMessage(MimeMessage mimeMessage, T message) throws MessagingException;
}
