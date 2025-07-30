package maeilmail.mail;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
@RequiredArgsConstructor
public class MailViewRenderer {

    private final SpringTemplateEngine templateEngine;

    public String render(Map<Object, Object> attribute, String template) {
        Context context = new Context();
        attribute.forEach((key, value) -> context.setVariable(key.toString(), value));

        return templateEngine.process(template, context);
    }
}
