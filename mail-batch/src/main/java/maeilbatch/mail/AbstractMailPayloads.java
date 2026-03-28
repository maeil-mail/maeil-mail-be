package maeilbatch.mail;

import java.util.List;
import maeilbatch.forward.ForwardLog;

public abstract class AbstractMailPayloads<T extends AbstractMailPayload> {

    protected final List<T> payloads;

    protected AbstractMailPayloads(List<T> payloads) {
        this.payloads = payloads;
    }

    public boolean isEmpty() {
        return payloads.isEmpty();
    }

    public List<ForwardLog> toForwardLogs() {
        return payloads.stream()
                .map(AbstractMailPayload::toForwardLog)
                .toList();
    }
}
