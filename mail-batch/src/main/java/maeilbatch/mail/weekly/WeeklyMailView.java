package maeilbatch.mail.weekly;

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
public class WeeklyMailView implements MailView {

    private final MailViewRenderer renderer;
    private final Subscribe subscribe;
    private final List<QuestionSummary> questionSummaries;
    private final LocalDate date;

    @Override
    public String render() {
        HashMap<Object, Object> attribute = new HashMap<>();
        String category = subscribe.getCategory().getDescription();
        int weekOfMonth = DateUtils.getWeekOfMonth(date);
        attribute.put("questions", questionSummaries);
        attribute.put("category", subscribe.getCategory().toLowerCase());
        attribute.put("email", subscribe.getEmail());
        attribute.put("token", subscribe.getToken());
        attribute.put("weekLabel", category + " " + date.getMonthValue() + "월 " + weekOfMonth + "주차 질문");
        attribute.put("year", date.getYear());
        attribute.put("month", date.getMonthValue());
        attribute.put("week", weekOfMonth);

        return renderer.render(attribute, "weekly-question");
    }

    @Override
    public String getType() {
        return "question";
    }
}
