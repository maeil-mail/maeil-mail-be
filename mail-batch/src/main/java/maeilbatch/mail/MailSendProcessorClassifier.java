package maeilbatch.mail;

import static maeilmail.subscribe.command.domain.SubscribeFrequency.DAILY;

import lombok.RequiredArgsConstructor;
import maeilmail.subscribe.command.domain.Subscribe;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.classify.Classifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailSendProcessorClassifier implements Classifier<Subscribe, ItemProcessor<?, ? extends AbstractMailPayload>> {

    private final ItemProcessor<Subscribe, AbstractMailPayload> dailyMailSendProcessor;
    private final ItemProcessor<Subscribe, AbstractMailPayload> weeklyMailSendProcessor;

    @Override
    public ItemProcessor<Subscribe, ? extends AbstractMailPayload> classify(Subscribe classifiable) {
        if (DAILY.equals(classifiable.getFrequency())) {
            return dailyMailSendProcessor;
        }

        return weeklyMailSendProcessor;
    }
}
