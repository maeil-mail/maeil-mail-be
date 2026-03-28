package maeilbatch.mail.daily;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilbatch.mail.AbstractMailPayload;
import maeilbatch.mail.ChoiceQuestionPolicy;
import maeilmail.mail.MailView;
import maeilmail.mail.MailViewRenderer;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.command.domain.Subscribe;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyMailSendProcessor implements ItemProcessor<Subscribe, AbstractMailPayload> {

    private final ChoiceQuestionPolicy choiceQuestionPolicy;
    private final MailViewRenderer mailViewRenderer;

    @Override
    public DailyMailPayload process(Subscribe subscribe) {
        try {
            return createDailyMailPayload(subscribe);
        } catch (Exception e) {
            log.error("일간 면접 질문 선택 실패. 구독자 id = {}", subscribe.getId(), e);
            return null;
        }
    }

    private DailyMailPayload createDailyMailPayload(Subscribe subscribe) {
        QuestionSummary questionSummary = choiceQuestionPolicy.choice(subscribe);
        MailView view = createView(subscribe, questionSummary);
        String subject = questionSummary.title();
        String text = view.render();

        return new DailyMailPayload(subscribe, questionSummary.toQuestion(), subject, text);
    }

    private DailyMailView createView(Subscribe subscribe, QuestionSummary question) {
        return DailyMailView.builder()
                .renderer(mailViewRenderer)
                .subscribe(subscribe)
                .question(question.toQuestion())
                .build();
    }
}
