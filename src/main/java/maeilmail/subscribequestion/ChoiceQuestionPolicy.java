package maeilmail.subscribequestion;

import java.time.LocalDate;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.Subscribe;

public interface ChoiceQuestionPolicy {

    QuestionSummary choice(Subscribe subscribe, LocalDate today);
}
