package maeilbatch.mail.weekly;

import static maeilmail.subscribe.command.domain.SubscribeFrequency.WEEKLY;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilbatch.mail.AbstractMailPayload;
import maeilbatch.mail.ChoiceQuestionPolicy;
import maeilmail.mail.MailView;
import maeilmail.mail.MailViewRenderer;
import maeilmail.question.Question;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.utils.DateUtils;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@StepScope
@Component
@RequiredArgsConstructor
class WeeklyMailSendProcessor implements ItemProcessor<Subscribe, AbstractMailPayload> {

    private static final String WEEKLY_MAIL_SUBJECT = "이번주 면접 질문을 보내드려요.";

    private final ChoiceQuestionPolicy choiceQuestionPolicy;
    private final MailViewRenderer mailViewRenderer;

    @Value("#{jobParameters['datetime']}")
    private LocalDateTime dateTime;

    @Override
    public WeeklyMailPayload process(Subscribe subscribe) {
        try {
            if (isNotSendDate()) {
                return null;
            }
            return createWeeklyMailPayload(subscribe);
        } catch (Exception e) {
            log.error("주간 면접 질문 선택 실패. 구독자 id = {}", subscribe.getId(), e);
            return null;
        }
    }

    private boolean isNotSendDate() {
        return !DateUtils.isMonday(dateTime.toLocalDate());
    }

    private WeeklyMailPayload createWeeklyMailPayload(Subscribe subscribe) {
        List<QuestionSummary> questions = choiceWeeklyQuestions(subscribe);
        MailView view = createView(subscribe, questions);
        String text = view.render();

        return createWeeklyMailPayload(subscribe, questions, WEEKLY_MAIL_SUBJECT, text);
    }

    private List<QuestionSummary> choiceWeeklyQuestions(Subscribe subscribe) {
        return IntStream.range(0, WEEKLY.getSendCount())
                .mapToObj(round -> choiceQuestionPolicy.choiceByRound(subscribe, round))
                .toList();
    }

    private MailView createView(Subscribe subscribe, List<QuestionSummary> questionSummaries) {
        return WeeklyMailView.builder()
                .renderer(mailViewRenderer)
                .date(dateTime.toLocalDate())
                .subscribe(subscribe)
                .questionSummaries(questionSummaries)
                .build();
    }

    public WeeklyMailPayload createWeeklyMailPayload(
            Subscribe subscribe,
            List<QuestionSummary> summaries,
            String subject,
            String text
    ) {
        List<Question> questions = summaries.stream()
                .map(QuestionSummary::toQuestion)
                .toList();

        return new WeeklyMailPayload(subscribe, questions, subject, text);
    }
}
