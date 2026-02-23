package maeilbatch.mail.daily;

import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilbatch.forward.ForwardLog;
import maeilbatch.forward.ForwardRepository;
import maeilmail.mail.MailMessage;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import maeilmail.subscribe.command.domain.SubscribeQuestionRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyMailSendWriter implements ItemWriter<MailMessage> {

    private final SubscribeQuestionRepository subscribeQuestionRepository;
    private final ForwardRepository forwardRepository;

    @Override
    public void write(Chunk<? extends MailMessage> chunk) {
        List<? extends MailMessage> items = chunk.getItems();
        items.forEach(this::rollingHistory);
        items.forEach(this::saveSendLog);
    }

    private void rollingHistory(MailMessage message) {
        DailyMailMessage dailyMailMessage = (DailyMailMessage) message;
        subscribeQuestionRepository
                .findBySubscribeAndQuestion(dailyMailMessage.subscribe(), dailyMailMessage.question())
                .ifPresent(subscribeQuestionRepository::delete);

        subscribeQuestionRepository.save(SubscribeQuestion.success(dailyMailMessage.subscribe(), dailyMailMessage.question()));
    }

    private void saveSendLog(MailMessage message) {
        ForwardLog forwardLog = new ForwardLog(message.getTo(), message.getSubject(), message.getText());
        forwardRepository.save(forwardLog);
    }
}
