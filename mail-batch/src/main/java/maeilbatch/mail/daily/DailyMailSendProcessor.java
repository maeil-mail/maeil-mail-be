package maeilbatch.mail.daily;

import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.bulksend.sender.ChoiceQuestionPolicy;
import maeilmail.bulksend.sender.SubscribeQuestionMessage;
import maeilmail.bulksend.view.SubscribeQuestionView;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.command.domain.Subscribe;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyMailSendProcessor implements ItemProcessor<Subscribe, SubscribeQuestionMessage> {

    private final ChoiceQuestionPolicy choiceQuestionPolicy;
    private final SubscribeQuestionView subscribeQuestionView;

    @Override
    public SubscribeQuestionMessage process(Subscribe subscribe) {
        try {
            return createSubscribeMessage(subscribe);
        } catch (Exception e) {
            log.error("일간 면접 질문 선택 실패. 구독자 id = {}", subscribe.getId(), e);
            return null;
        }
    }

    private SubscribeQuestionMessage createSubscribeMessage(Subscribe subscribe) {
        QuestionSummary questionSummary = choiceQuestionPolicy.choice(subscribe);
        String subject = createSubject(questionSummary);
        String text = createText(subscribe, questionSummary);

        return new SubscribeQuestionMessage(subscribe, questionSummary.toQuestion(), subject, text);
    }

    private String createSubject(QuestionSummary question) {
        return question.title();
    }

    private String createText(Subscribe subscribe, QuestionSummary question) {
        HashMap<Object, Object> attribute = new HashMap<>();
        attribute.put("questionId", question.id());
        attribute.put("question", question.title());
        attribute.put("email", subscribe.getEmail());
        attribute.put("token", subscribe.getToken());

        return subscribeQuestionView.render(attribute);
    }
}
