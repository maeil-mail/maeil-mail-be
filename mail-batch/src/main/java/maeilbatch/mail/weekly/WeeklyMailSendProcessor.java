package maeilbatch.mail.weekly;

import static maeilmail.subscribe.command.domain.SubscribeFrequency.WEEKLY;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.bulksend.sender.ChoiceQuestionPolicy;
import maeilmail.mail.MailMessage;
import maeilmail.mail.MailView;
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
class WeeklyMailSendProcessor implements ItemProcessor<Subscribe, MailMessage> {

    private static final String WEEKLY_MAIL_SUBJECT = "이번주 면접 질문을 보내드려요.";

    private final ChoiceQuestionPolicy choiceQuestionPolicy;

    @Value("#{jobParameters['datetime']}")
    private LocalDateTime dateTime;

    @Override
    public WeeklyMailMessage process(Subscribe subscribe) {
        try {
            if (isNotSendDate()) {
                return null;
            }
            return createWeeklyMailMessage(subscribe);
        } catch (Exception e) {
            log.error("주간 면접 질문 선택 실패. 구독자 id = {}", subscribe.getId(), e);
            return null;
        }
    }

    private boolean isNotSendDate() {
        return !DateUtils.isMonday(dateTime.toLocalDate());
    }

    private WeeklyMailMessage createWeeklyMailMessage(Subscribe subscribe) {
        List<QuestionSummary> questions = choiceWeeklyQuestions(subscribe);
        MailView view = createView(subscribe, questions);
        String text = view.render();

        return createWeeklyMailMessage(subscribe, questions, WEEKLY_MAIL_SUBJECT, text);
    }

    private MailView createView(Subscribe subscribe, List<QuestionSummary> questionSummaries) {
        return WeeklyMailView.builder()
                .date(dateTime.toLocalDate())
                .subscribe(subscribe)
                .questionSummaries(questionSummaries)
                .build();
    }

    private List<QuestionSummary> choiceWeeklyQuestions(Subscribe subscribe) {
        return IntStream.range(0, WEEKLY.getSendCount())
                .mapToObj(round -> choiceQuestionPolicy.choiceByRound(subscribe, round))
                .toList();
    }

    public WeeklyMailMessage createWeeklyMailMessage(
            Subscribe subscribe,
            List<QuestionSummary> summaries,
            String subject,
            String text
    ) {
        List<Question> questions = summaries.stream()
                .map(QuestionSummary::toQuestion)
                .toList();

        return new WeeklyMailMessage(subscribe, questions, subject, text);
    }
}
