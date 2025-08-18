package maeilmail.bulksend.view;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import lombok.Builder;
import maeilmail.mail.MailView;
import maeilmail.mail.MailViewRenderer;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.utils.DateUtils;

@Builder
public class WeeklySubscribeQuestionView implements MailView {

    private final MailViewRenderer renderer;
    private final Subscribe subscribe;
    private final List<QuestionSummary> questionSummaries;
    private final LocalDate date;

    @Override
    public String render() {
        HashMap<Object, Object> attributes = new HashMap<>();
        String category = subscribe.getCategory().getDescription();
        int weekOfMonth = DateUtils.getWeekOfMonth(date);
        attributes.put("questions", questionSummaries);
        attributes.put("category", subscribe.getCategory().toLowerCase());
        attributes.put("email", subscribe.getEmail());
        attributes.put("token", subscribe.getToken());
        attributes.put("weekLabel", category + " " + date.getMonthValue() + "월 " + weekOfMonth + "주차 질문");
        attributes.put("year", date.getYear());
        attributes.put("month", date.getMonthValue());
        attributes.put("week", weekOfMonth);

        return renderer.render(attributes, "weekly-question");
    }

    @Override
    public String getType() {
        return "question";
    }
}
