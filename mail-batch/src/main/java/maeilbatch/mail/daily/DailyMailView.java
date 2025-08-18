package maeilbatch.mail.daily;

import java.util.HashMap;
import lombok.Builder;
import maeilmail.mail.MailView;
import maeilmail.mail.MailViewRenderer;
import maeilmail.question.Question;
import maeilmail.subscribe.command.domain.Subscribe;

@Builder
public class DailyMailView implements MailView {

    private final MailViewRenderer renderer;
    private final Subscribe subscribe;
    private final Question question;

    @Override
    public String render() {
        HashMap<Object, Object> attribute = new HashMap<>();
        attribute.put("questionId", question.getId());
        attribute.put("question", question.getTitle());
        attribute.put("email", subscribe.getEmail());
        attribute.put("token", subscribe.getToken());

        return renderer.render(attribute, "question-v4");
    }

    @Override
    public String getType() {
        return "question";
    }
}
