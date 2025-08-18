package maeilbatch.mail.daily;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import maeilmail.mail.MailView;
import maeilmail.mail.MailViewRenderer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyMailView implements MailView {

    private final MailViewRenderer mailViewRenderer;

    @Override
    public String render(Map<Object, Object> attribute) {
        return mailViewRenderer.render(attribute, "question-v4");
    }

    @Override
    public String getType() {
        return "question";
    }
}
