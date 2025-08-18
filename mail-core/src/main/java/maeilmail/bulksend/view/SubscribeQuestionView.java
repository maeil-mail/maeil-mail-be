package maeilmail.bulksend.view;

import java.util.HashMap;
import lombok.Builder;
import maeilmail.mail.MailView;
import maeilmail.mail.MailViewRenderer;
import maeilmail.question.Question;
import maeilmail.subscribe.command.domain.Subscribe;

@Builder
public class SubscribeQuestionView implements MailView {

    private final MailViewRenderer renderer;
    private final Subscribe subscribe;
    private final Question question;

    @Override
    public String render() {
        HashMap<Object, Object> attributes = new HashMap<>();
        attributes.put("questionId", question.getId());
        attributes.put("question", question.getTitle());
        attributes.put("email", subscribe.getEmail());
        attributes.put("token", subscribe.getToken());

        return renderer.render(attributes, "question-v4");
    }

    @Override
    public String getType() {
        return "question";
    }
}
