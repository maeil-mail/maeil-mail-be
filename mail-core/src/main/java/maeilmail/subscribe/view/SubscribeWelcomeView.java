package maeilmail.subscribe.view;

import java.util.Map;
import lombok.Builder;
import maeilmail.mail.MailView;
import maeilmail.mail.MailViewRenderer;

@Builder
public class SubscribeWelcomeView implements MailView {

    private final MailViewRenderer renderer;

    @Override
    public String render() {
        return renderer.render(Map.of(), "subscribe-welcome");
    }

    @Override
    public String getType() {
        return "welcome";
    }
}
