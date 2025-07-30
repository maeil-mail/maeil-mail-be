package maeilmail.bulksend.sender;

import maeilmail.mail.MimeMessageCreator;
import maeilmail.subscribe.command.domain.Subscribe;
import org.springframework.stereotype.Component;

@Component
public class QuestionMimeMessageCreator extends MimeMessageCreator<SubscribeQuestionMessage> {

    @Override
    public String extractTo(SubscribeQuestionMessage message) {
        Subscribe subscribe = message.subscribe();

        return subscribe.getEmail();
    }

    @Override
    public String extractText(SubscribeQuestionMessage message) {
        return message.text();
    }

    @Override
    public String extractSubject(SubscribeQuestionMessage message) {
        return message.subject();
    }
}
