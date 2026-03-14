package maeilbatch.forward;

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
        forwardDao.changeState(chunk.getItems(), ForwardStatus.PROCESSING);
        chunk.forEach(forwardSender::sendMailSync);
    }
}
