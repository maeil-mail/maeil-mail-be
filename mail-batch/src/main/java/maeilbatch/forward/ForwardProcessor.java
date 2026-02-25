package maeilbatch.forward;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class ForwardProcessor implements ItemProcessor<ForwardLog, ForwardLog> {

    @Override
    public ForwardLog process(ForwardLog item) {
        if (!item.isRetryable()) {
            return null;
        }

        return item;
    }
}
