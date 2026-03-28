package maeilbatch.forward;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ForwardProcessor implements ItemProcessor<ForwardLog, ForwardLog> {

    @Override
    public ForwardLog process(ForwardLog item) {
        if (!item.isRetryable()) {
            log.info("메일 전송 호출을 식별할 수 없어 질문지를 전송할 수 없습니다. email = {} status = {}", item.getTarget(), item.getStatus());
            return null;
        }

        return item;
    }
}
