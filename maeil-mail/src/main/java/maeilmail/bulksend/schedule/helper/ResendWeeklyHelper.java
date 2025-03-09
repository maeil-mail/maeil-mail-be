package maeilmail.bulksend.schedule.helper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import maeilmail.bulksend.sender.WeeklyQuestionSender;
import maeilmail.bulksend.sender.WeeklySubscribeQuestionMessage;
import maeilmail.bulksend.view.WeeklySubscribeQuestionView;
import maeilmail.question.Question;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import maeilsupport.DateUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResendWeeklyHelper {

    private final WeeklySubscribeQuestionView weeklySubscribeQuestionView;
    private final WeeklyQuestionSender weeklyQuestionSender;

    public void resend(List<List<SubscribeQuestion>> weeklyTargets) {
        weeklyTargets.stream()
                .map(this::generateWeeklyQuestionMessage)
                .filter(Objects::nonNull)
                .forEach(weeklyQuestionSender::sendMail);
    }

    private WeeklySubscribeQuestionMessage generateWeeklyQuestionMessage(List<SubscribeQuestion> subscribeQuestions) {
        if (subscribeQuestions.size() != 5) {
            return null;
        }

        String subject = "이번주 면접 질문을 보내드려요.";
        Subscribe subscribe = subscribeQuestions.get(0).getSubscribe();
        List<QuestionSummary> questions = subscribeQuestions.stream()
                .map(SubscribeQuestion::getQuestion)
                .map(this::toQuestionSummary)
                .sorted((o1, o2) -> Math.toIntExact(o1.id() - o2.id()))
                .toList();

        return createWeeklySubscribeQuestionMessage(subscribe, questions, subject, createText(subscribe, questions));
    }

    private QuestionSummary toQuestionSummary(Question it) {
        return new QuestionSummary(
                it.getId(),
                it.getTitle(),
                it.getContent(),
                it.getCategory().toLowerCase(),
                it.getCreatedAt(),
                it.getUpdatedAt()
        );
    }

    private String createText(Subscribe subscribe, List<QuestionSummary> questions) {
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

    private WeeklySubscribeQuestionMessage createWeeklySubscribeQuestionMessage(
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
