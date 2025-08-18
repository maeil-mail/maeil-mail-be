package maeilbatch.mail.daily;

import maeilmail.mail.MailMessage;
import maeilmail.question.Question;
import maeilmail.subscribe.command.domain.Subscribe;

public record DailyMailMessage(
        Subscribe subscribe,
        Question question,
        String subject,
        String text
) implements MailMessage {

    @Override
    public String getTo() {
        return subscribe.getEmail();
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getSubject() {
        return subject;
    }
}
