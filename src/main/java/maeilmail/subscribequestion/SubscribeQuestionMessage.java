package maeilmail.subscribequestion;

import maeilmail.question.Question;
import maeilmail.subscribe.core.Subscribe;

public record SubscribeQuestionMessage(
        Subscribe subscribe,
        Question question,
        String subject,
        String text
) {
}
