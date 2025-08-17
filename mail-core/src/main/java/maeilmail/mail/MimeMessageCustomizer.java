package maeilmail.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class MimeMessageCustomizer {

    private static final String FROM_EMAIL = "maeil-mail <noreply@maeil-mail.kr>";
    private static final String TITLE_PREFIX = "[매일메일] %s";

    public MimeMessage customize(MimeMessage mimeMessage, MailMessage message) throws MessagingException {
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setFrom(FROM_EMAIL);
        helper.setTo(message.getTo());
        helper.setSubject(String.format(TITLE_PREFIX, message.getSubject()));
        helper.setText(message.getText(), true);

        return mimeMessage;
    }
}
