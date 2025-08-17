package maeilbatch.mail;

import static maeilmail.subscribe.command.domain.SubscribeFrequency.DAILY;

import lombok.RequiredArgsConstructor;
import maeilmail.mail.MailMessage;
import maeilmail.subscribe.command.domain.Subscribe;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.classify.Classifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class MailSendProcessorClassifier implements Classifier<Subscribe, ItemProcessor<?, ? extends MailMessage>> {

    private final ItemProcessor<Subscribe, MailMessage> dailyMailSendProcessor;
    private final ItemProcessor<Subscribe, MailMessage> weeklyMailSendProcessor;

    @Override
    public ItemProcessor<Subscribe, ? extends MailMessage> classify(Subscribe classifiable) {
        if (DAILY.equals(classifiable.getFrequency())) {
            return dailyMailSendProcessor;
        }

        return weeklyMailSendProcessor;
    }
}
