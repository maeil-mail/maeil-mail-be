package maeilbatch.mail.weekly;

import static maeilmail.subscribe.command.domain.SubscribeFrequency.WEEKLY;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.bulksend.sender.ChoiceQuestionPolicy;
import maeilmail.bulksend.sender.WeeklySubscribeQuestionMessage;
import maeilmail.bulksend.view.WeeklySubscribeQuestionView;
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
class WeeklyMailSendProcessor implements ItemProcessor<Subscribe, WeeklySubscribeQuestionMessage> {

    private final ChoiceQuestionPolicy choiceQuestionPolicy;
    private final WeeklySubscribeQuestionView weeklySubscribeQuestionView;

    @Value("#{jobParameters['datetime']}")
    private LocalDateTime dateTime;

    @Override
    public WeeklySubscribeQuestionMessage process(Subscribe subscribe) {
        try {
            if (isNotSendDate()) {
                return null;
            }
            return createWeeklySubscribeMessage(subscribe);
        } catch (Exception e) {
            log.error("주간 면접 질문 선택 실패. 구독자 id = {}", subscribe.getId(), e);
            return null;
        }
    }

    private boolean isNotSendDate() {
        return !DateUtils.isMonday(dateTime.toLocalDate());
    }

    private WeeklySubscribeQuestionMessage createWeeklySubscribeMessage(Subscribe subscribe) {
        List<QuestionSummary> questions = choiceWeeklyQuestions(subscribe);
        String subject = createSubject();
        String text = createText(subscribe, questions);

        return createWeeklySubscribeQuestionMessage(subscribe, questions, subject, text);
    }

    private List<QuestionSummary> choiceWeeklyQuestions(Subscribe subscribe) {
        return IntStream.range(0, WEEKLY.getSendCount())
                .mapToObj(round -> choiceQuestionPolicy.choiceByRound(subscribe, round))
                .toList();
    }

    private String createSubject() {
        return "이번주 면접 질문을 보내드려요.";
    }

    public String createText(Subscribe subscribe, List<QuestionSummary> questions) {
        LocalDate today = LocalDate.now();
        HashMap<Object, Object> attribute = new HashMap<>();
        String category = subscribe.getCategory().getDescription();
        int weekOfMonth = DateUtils.getWeekOfMonth(today);
        attribute.put("questions", questions);
        attribute.put("category", subscribe.getCategory().toLowerCase());
        attribute.put("email", subscribe.getEmail());
        attribute.put("token", subscribe.getToken());
        attribute.put("weekLabel", category + " " + today.getMonthValue() + "월 " + weekOfMonth + "주차 질문");
        attribute.put("year", today.getYear());
        attribute.put("month", today.getMonthValue());
        attribute.put("week", weekOfMonth);

        return weeklySubscribeQuestionView.render(attribute);
    }

    public WeeklySubscribeQuestionMessage createWeeklySubscribeQuestionMessage(
            Subscribe subscribe,
            List<QuestionSummary> summaries,
            String subject,
            String text
    ) {
        List<Question> questions = summaries.stream()
                .map(QuestionSummary::toQuestion)
                .toList();

        return new WeeklySubscribeQuestionMessage(subscribe, questions, subject, text);
    }
}
