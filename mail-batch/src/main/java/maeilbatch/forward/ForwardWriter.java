package maeilbatch.forward;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ForwardWriter implements ItemWriter<ForwardLog> {

    private final ForwardDao forwardDao;
    private final ForwardSender forwardSender;

    @Override
    public void write(Chunk<? extends ForwardLog> chunk) {
        if (chunk.isEmpty()) return;
        forwardDao.changeStateWithNewTx(chunk.getItems(), ForwardStatus.PROCESSING);
        chunk.forEach(forwardSender::sendMailSync);
        bulkWrite(chunk);
    }

    private void bulkWrite(Chunk<? extends ForwardLog> chunk) {
        List<ForwardStatus> targets = List.of(ForwardStatus.DONE, ForwardStatus.FAILED);

        targets.forEach(it -> {
            List<? extends ForwardLog> logs = getLogs(chunk, it);
            forwardDao.changeState(logs, it);
        });
    }

    private List<? extends ForwardLog> getLogs(Chunk<? extends ForwardLog> chunk, ForwardStatus target) {
        List<? extends ForwardLog> items = chunk.getItems();

        return items.stream()
                .filter(it -> it.getStatus() == target)
                .toList();
    }
}
