package maeilmail.subscribe.core;

import java.time.LocalDate;
import maeilmail.question.QuestionSummary;

public interface ChoiceQuestionPolicy {

    QuestionSummary choice(Subscribe subscribe, LocalDate today);
}
