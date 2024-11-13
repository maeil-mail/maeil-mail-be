package maeilmail.subscribe;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import maeilmail.mail.MailView;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
@RequiredArgsConstructor
class SubscribeWelcomeView implements MailView {

    private final SpringTemplateEngine templateEngine;

    @Override
    public String render(Map<Object, Object> attribute) {
        Context context = new Context();
        return templateEngine.process("subscribe-welcome", context);
    }

    @Override
    public String getType() {
        return "welcome";
    }
}
