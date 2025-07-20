package maeilmail.admin.view;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import maeilmail.mail.MailView;
import maeilmail.mail.MailViewRenderer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminReportView implements MailView {

    private final MailViewRenderer mailViewRenderer;

    @Override
    public String render(Map<Object, Object> attribute) {
        return mailViewRenderer.render(attribute, "report");
    }

    @Override
    public String getType() {
        return "report";
    }
}
