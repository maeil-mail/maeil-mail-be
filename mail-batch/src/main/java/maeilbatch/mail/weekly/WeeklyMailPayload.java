package maeilbatch.mail.weekly;

import java.util.List;
import lombok.Getter;
import maeilbatch.mail.AbstractMailPayload;
import maeilmail.question.Question;
import maeilmail.subscribe.command.domain.Subscribe;

@Getter
public class WeeklyMailPayload extends AbstractMailPayload {

    private final List<Question> questions;

    public WeeklyMailPayload(Subscribe subscribe, List<Question> questions, String subject, String text) {
        super(subscribe, subject, text);

        this.questions = questions;
    }
}
