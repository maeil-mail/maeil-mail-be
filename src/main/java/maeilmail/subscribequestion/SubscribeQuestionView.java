package maeilmail.subscribequestion;

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
        attribute.forEach((key, value) -> context.setVariable(key.toString(), value.toString()));

        return templateEngine.process("question-v3", context);
    }

    @Override
    public String getType() {
        return "question";
    }
}
