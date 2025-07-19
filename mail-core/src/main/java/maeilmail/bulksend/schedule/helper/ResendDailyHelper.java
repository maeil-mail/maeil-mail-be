package maeilmail.bulksend.schedule.helper;

import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilmail.bulksend.sender.QuestionSender;
import maeilmail.bulksend.sender.SubscribeQuestionMessage;
import maeilmail.bulksend.view.SubscribeQuestionView;
import maeilmail.question.Question;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResendDailyHelper {

    private final SubscribeQuestionView subscribeQuestionView;
    private final QuestionSender questionSender;

    public void resend(List<SubscribeQuestion> dailyTargets) {
        dailyTargets.stream()
                .map(this::generateQuestionMessage)
                .forEach(questionSender::sendMail);
    }

    private SubscribeQuestionMessage generateQuestionMessage(SubscribeQuestion subscribeQuestion) {
        Subscribe subscribe = subscribeQuestion.getSubscribe();
        Question question = subscribeQuestion.getQuestion();
        String subject = question.getTitle();
        String text = createText(subscribe, question);

        return new SubscribeQuestionMessage(subscribe, question, subject, text);
    }

    private String createText(Subscribe subscribe, Question question) {
        HashMap<Object, Object> attribute = new HashMap<>();
        attribute.put("questionId", question.getId());
        attribute.put("question", question.getTitle());
        attribute.put("email", subscribe.getEmail());
        attribute.put("token", subscribe.getToken());

        return subscribeQuestionView.render(attribute);
    }
}
