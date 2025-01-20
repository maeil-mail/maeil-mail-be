package maeilmail.admin;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import maeilmail.mail.MailView;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
@RequiredArgsConstructor
class AdminReportView implements MailView {

    private final SpringTemplateEngine templateEngine;

    @Override
    public String render(Map<Object, Object> attribute) {
        Context context = new Context();
        context.setVariable("report", attribute.get("report"));

        return templateEngine.process("report", context);
    }

    @Override
    public String getType() {
        return "report";
    }
}
