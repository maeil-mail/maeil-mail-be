package maeilmail.bulksend.sender;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import maeilmail.mail.MimeMessageCreator;
import maeilmail.subscribe.command.domain.Subscribe;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class QuestionMimeMessageCreator extends MimeMessageCreator<SubscribeQuestionMessage> {

    @Override
    public MimeMessage createMimeMessage(MimeMessage mimeMessage, SubscribeQuestionMessage message) throws MessagingException {
        Subscribe subscribe = message.subscribe();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setFrom(FROM_EMAIL);
        helper.setTo(subscribe.getEmail());
        helper.setSubject(String.format(TITLE_PREFIX, message.subject()));
        helper.setText(message.text(), true);
        return mimeMessage;
    }
}
