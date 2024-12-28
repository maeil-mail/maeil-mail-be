package maeilmail.subscribequestion;

import maeilmail.question.Question;
import maeilmail.subscribe.command.domain.Subscribe;

public record SubscribeQuestionMessage(
        Subscribe subscribe,
        Question question,
        String subject,
        String text
) {
}
