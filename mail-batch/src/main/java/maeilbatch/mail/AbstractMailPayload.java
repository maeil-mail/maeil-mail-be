package maeilbatch.mail;

import lombok.Getter;
import maeilbatch.forward.ForwardLog;
import maeilmail.subscribe.command.domain.Subscribe;

@Getter
public abstract class AbstractMailPayload {

    private final Subscribe subscribe;
    private final String text;
    private final String subject;

    protected AbstractMailPayload(Subscribe subscribe, String subject, String text) {
        this.subscribe = subscribe;
        this.subject = subject;
        this.text = text;
    }

    public ForwardLog toForwardLog() {
        return new ForwardLog(getSubscribe().getEmail(), getSubject(), getText());
    }
}
