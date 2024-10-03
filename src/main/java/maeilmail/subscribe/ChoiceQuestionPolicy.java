package maeilmail.subscribe;

import java.time.LocalDate;
import maeilmail.question.Question;

public interface ChoiceQuestionPolicy {

    Question choice(Subscribe subscribe, LocalDate today);
}
