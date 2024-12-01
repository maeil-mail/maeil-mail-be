package maeilmail.subscribequestion;

import java.util.List;
import maeilmail.question.Question;
import maeilmail.subscribe.Subscribe;

public record WeeklySubscribeQuestionMessage(
        Subscribe subscribe,
        List<Question> questions,
        String subject,
        String text
) {
}
