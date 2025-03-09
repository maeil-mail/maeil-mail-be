package maeilmail.bulksend.schedule.helper;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import maeilmail.bulksend.schedule.SendWeeklyQuestionScheduler;
import maeilmail.bulksend.sender.WeeklyQuestionSender;
import maeilmail.bulksend.sender.WeeklySubscribeQuestionMessage;
import maeilmail.question.Question;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResendWeeklyHelper {

    private final WeeklyQuestionSender weeklyQuestionSender;
    private final SendWeeklyQuestionScheduler sendWeeklyQuestionScheduler;

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

        String text = sendWeeklyQuestionScheduler.createText(subscribe, questions);

        return sendWeeklyQuestionScheduler.createWeeklySubscribeQuestionMessage(subscribe, questions, subject, text);
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
}
