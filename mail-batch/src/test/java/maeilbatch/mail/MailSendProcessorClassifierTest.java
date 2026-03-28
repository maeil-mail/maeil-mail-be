package maeilbatch.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ItemProcessor;

class MailSendProcessorClassifierTest {

    @Test
    @DisplayName("구독 주기에 따라 적절한 processor를 분기한다.")
    void classify() {
        ItemProcessor<Subscribe, AbstractMailPayload> dailyProcessor = createMockProcessor();
        ItemProcessor<Subscribe, AbstractMailPayload> weeklyProcessor = createMockProcessor();
        MailSendProcessorClassifier classifier = new MailSendProcessorClassifier(dailyProcessor, weeklyProcessor);

        ItemProcessor<Subscribe, ? extends AbstractMailPayload> dailyResult = classifier.classify(createDailySubscribe());
        ItemProcessor<Subscribe, ? extends AbstractMailPayload> weeklyResult = classifier.classify(createWeeklySubscribe());

        assertAll(
                () -> assertThat(dailyResult).isSameAs(dailyProcessor),
                () -> assertThat(weeklyResult).isSameAs(weeklyProcessor)
        );
    }

    @SuppressWarnings("unchecked")
    private ItemProcessor<Subscribe, AbstractMailPayload> createMockProcessor() {
        return mock(ItemProcessor.class);
    }

    private Subscribe createDailySubscribe() {
        return new Subscribe("daily@test.com", QuestionCategory.BACKEND, SubscribeFrequency.DAILY);
    }

    private Subscribe createWeeklySubscribe() {
        return new Subscribe("weekly@test.com", QuestionCategory.BACKEND, SubscribeFrequency.WEEKLY);
    }
}
