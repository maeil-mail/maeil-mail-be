package maeilmail.bulksend;

import java.time.LocalDate;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.command.domain.Subscribe;

public interface ChoiceQuestionPolicy {

    QuestionSummary choice(Subscribe subscribe, LocalDate today);
}
