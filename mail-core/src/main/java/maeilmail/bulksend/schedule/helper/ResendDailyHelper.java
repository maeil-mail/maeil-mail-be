package maeilmail.bulksend.schedule.helper;

import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilmail.bulksend.sender.QuestionSender;
import maeilmail.bulksend.sender.SubscribeQuestionMessage;
import maeilmail.bulksend.view.SubscribeQuestionView;
import maeilmail.mail.MailViewRenderer;
import maeilmail.question.Question;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResendDailyHelper {

    private final QuestionSender questionSender;
    private final MailViewRenderer mailViewRenderer;

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
        SubscribeQuestionView view = SubscribeQuestionView.builder()
                .renderer(mailViewRenderer)
                .subscribe(subscribe)
                .question(question)
                .build();

        return view.render();
    }
}
