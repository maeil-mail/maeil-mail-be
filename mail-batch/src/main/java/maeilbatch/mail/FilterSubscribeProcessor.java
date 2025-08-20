package maeilbatch.mail;

import lombok.extern.slf4j.Slf4j;
import maeilmail.subscribe.command.domain.Subscribe;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class FilterSubscribeProcessor implements ItemProcessor<Subscribe, Subscribe> {

    @Override
    public Subscribe process(Subscribe subscribe) {
        if (subscribe.getDeletedAt() == null) {
            return subscribe;
        }

        return null;
    }
}
