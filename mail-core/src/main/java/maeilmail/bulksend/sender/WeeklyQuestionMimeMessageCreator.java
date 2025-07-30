package maeilmail.bulksend.sender;

import maeilmail.mail.MimeMessageCreator;
import maeilmail.subscribe.command.domain.Subscribe;
import org.springframework.stereotype.Component;

@Component
public class WeeklyQuestionMimeMessageCreator extends MimeMessageCreator<WeeklySubscribeQuestionMessage> {

    @Override
    public String extractTo(WeeklySubscribeQuestionMessage message) {
        Subscribe subscribe = message.subscribe();

        return subscribe.getEmail();
    }

    @Override
    public String extractText(WeeklySubscribeQuestionMessage message) {
        return message.text();
    }

    @Override
    public String extractSubject(WeeklySubscribeQuestionMessage message) {
        return message.subject();
    }
}
