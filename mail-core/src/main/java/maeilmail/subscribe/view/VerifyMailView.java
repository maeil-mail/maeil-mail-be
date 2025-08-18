package maeilmail.subscribe.view;

import java.util.Map;
import lombok.Builder;
import maeilmail.mail.MailView;
import maeilmail.mail.MailViewRenderer;

@Builder
public class VerifyMailView implements MailView {

    private final MailViewRenderer renderer;
    private final String verifyCode;

    @Override
    public String render() {
        return renderer.render(Map.of("code", verifyCode), "verify-email-v2");
    }

    @Override
    public String getType() {
        return "verify";
    }
}
