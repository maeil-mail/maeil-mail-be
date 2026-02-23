package maeilbatch.forward;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * --- 별도 트랜잭션 (pending log 일괄 업데이트)
 * forwardlogs.forEach(it -> it.setStatus(PROCESSING) // 처리 중 상태로 마킹
 * <p>
 * --- 청크 트랜잭션 (메일 발송 시도) ---
 * messages = forwardlogs.map(this::mapToMessage) // 메시지로 변경
 * mailSender.send(messages); // 발송
 * - mailSender 내부에서 log 업데이트를 수행
 */
@Component
@RequiredArgsConstructor
public class ForwardWriter implements ItemWriter<ForwardLog> {

    private final StatusBatchChanger statusBatchChanger;
    private final ForwardSender forwardSender;
    private final EntityManager em;

    @Override
    public void write(Chunk<? extends ForwardLog> chunk) {
        statusBatchChanger.changeState(chunk.getItems(), ForwardStatus.PROCESSING);
        chunk.forEach(forwardSender::sendMailSync);
        em.flush();
    }
}
