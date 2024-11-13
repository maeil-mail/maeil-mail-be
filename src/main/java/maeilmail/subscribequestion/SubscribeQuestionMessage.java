package maeilmail.subscribequestion;

import maeilmail.question.Question;
import maeilmail.subscribe.Subscribe;

public record SubscribeQuestionMessage(
        Subscribe subscribe,
        Question question,
        String subject,
        String text
) {
}
