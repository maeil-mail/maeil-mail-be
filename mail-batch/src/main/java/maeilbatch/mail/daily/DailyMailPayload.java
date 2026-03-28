package maeilbatch.mail.daily;

import lombok.Getter;
import maeilbatch.mail.AbstractMailPayload;
import maeilmail.question.Question;
import maeilmail.subscribe.command.domain.Subscribe;

@Getter
public class DailyMailPayload extends AbstractMailPayload {

    private final Question question;

    public DailyMailPayload(Subscribe subscribe, Question question, String subject, String text) {
        super(subscribe, subject, text);

        this.question = question;
    }
}
