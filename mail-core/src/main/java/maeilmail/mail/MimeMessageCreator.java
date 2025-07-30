package maeilmail.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

public abstract class MimeMessageCreator<T> {

    protected static final String FROM_EMAIL = "maeil-mail <noreply@maeil-mail.kr>";
    protected static final String TITLE_PREFIX = "[매일메일] %s";

    public MimeMessage createMimeMessage(MimeMessage mimeMessage, T message) throws MessagingException {
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setFrom(FROM_EMAIL);
        helper.setTo(extractTo(message));
        helper.setSubject(String.format(TITLE_PREFIX, extractSubject(message)));
        helper.setText(extractText(message), true);

        return mimeMessage;
    }

    protected abstract String extractTo(T message);

    protected abstract String extractText(T message);

    protected abstract String extractSubject(T message);
}
