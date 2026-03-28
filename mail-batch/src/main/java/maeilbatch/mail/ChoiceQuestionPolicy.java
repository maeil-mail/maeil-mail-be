package maeilbatch.mail;

import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.command.domain.Subscribe;

public interface ChoiceQuestionPolicy {

    QuestionSummary choice(Subscribe subscribe);

    QuestionSummary choiceByRound(Subscribe subscribe, int round);
}
