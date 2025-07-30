package maeilmail.mail;

import org.springframework.stereotype.Component;

@Component
public class MailMimeMessageCustomizer extends MimeMessageCustomizer<MailMessage> {

    @Override
    public String extractTo(MailMessage message) {
        return message.to();
    }

    @Override
    public String extractText(MailMessage message) {
        return message.text();
    }

    @Override
    public String extractSubject(MailMessage message) {
        return message.subject();
    }
}
