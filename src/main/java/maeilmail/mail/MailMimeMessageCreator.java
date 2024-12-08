package maeilmail.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class MailMimeMessageCreator extends MimeMessageCreator<MailMessage> {

    @Override
    public MimeMessage createMimeMessage(MimeMessage mimeMessage, MailMessage message) throws MessagingException {
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setFrom(FROM_EMAIL);
        helper.setTo(message.to());
        helper.setSubject(String.format(TITLE_PREFIX, message.subject()));
        helper.setText(message.text(), true);
        return mimeMessage;
    }
}
