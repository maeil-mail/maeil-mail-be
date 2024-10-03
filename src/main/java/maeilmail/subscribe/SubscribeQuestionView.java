package maeilmail.subscribe;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import maeilmail.mail.MailView;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
@RequiredArgsConstructor
class SubscribeQuestionView implements MailView {

    private final SpringTemplateEngine templateEngine;

    @Override
    public String render(Map<Object, Object> attribute) {
        Context context = new Context();
        context.setVariable("questionId", attribute.get("questionId"));
        context.setVariable("question", attribute.get("question"));

        return templateEngine.process("question-v2", context);
    }

    @Override
    public String getType() {
        return "question";
    }
}
