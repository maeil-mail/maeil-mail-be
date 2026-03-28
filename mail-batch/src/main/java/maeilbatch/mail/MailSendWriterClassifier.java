package maeilbatch.mail;

import lombok.RequiredArgsConstructor;
import maeilbatch.mail.daily.DailyMailPayload;
import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailSendWriterClassifier implements Classifier<AbstractMailPayload, ItemWriter<? super AbstractMailPayload>> {

    private final ItemWriter<AbstractMailPayload> dailyMailSendWriter;
    private final ItemWriter<AbstractMailPayload> weeklyMailSendWriter;

    @Override
    public ItemWriter<? super AbstractMailPayload> classify(AbstractMailPayload classifiable) {
        if (classifiable instanceof DailyMailPayload) {
            return dailyMailSendWriter;
        }

        return weeklyMailSendWriter;
    }
}
