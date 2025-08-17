package maeilbatch.mail;

import lombok.RequiredArgsConstructor;
import maeilmail.bulksend.sender.SubscribeQuestionMessage;
import maeilmail.mail.MailMessage;
import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailSendWriterClassifier implements Classifier<MailMessage, ItemWriter<? super MailMessage>> {

    private final ItemWriter<MailMessage> dailyMailSendWriter;
    private final ItemWriter<MailMessage> weeklyMailSendWriter;

    @Override
    public ItemWriter<? super MailMessage> classify(MailMessage classifiable) {
        if (classifiable instanceof SubscribeQuestionMessage) {
            return dailyMailSendWriter;
        }

        return weeklyMailSendWriter;
    }
}
