package maeilbatch.mail.daily;

import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilbatch.forward.ForwardRepository;
import maeilbatch.mail.AbstractMailPayload;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import maeilmail.subscribe.command.domain.SubscribeQuestionRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyMailSendWriter implements ItemWriter<AbstractMailPayload> {

    private final SubscribeQuestionRepository subscribeQuestionRepository;
    private final ForwardRepository forwardRepository;

    @Override
    public void write(Chunk<? extends AbstractMailPayload> chunk) {
        List<? extends AbstractMailPayload> items = chunk.getItems();
        items.forEach(this::rollingHistory);
        items.forEach(this::saveSendLog);
    }

    private void rollingHistory(AbstractMailPayload payload) {
        DailyMailPayload dailyMailPayload = (DailyMailPayload) payload;
        subscribeQuestionRepository
                .findBySubscribeAndQuestion(dailyMailPayload.getSubscribe(), dailyMailPayload.getQuestion())
                .ifPresent(subscribeQuestionRepository::delete);

        subscribeQuestionRepository.save(SubscribeQuestion.success(
                dailyMailPayload.getSubscribe(),
                dailyMailPayload.getQuestion()
        ));
    }

    private void saveSendLog(AbstractMailPayload payload) {
        forwardRepository.save(payload.toForwardLog());
    }
}
