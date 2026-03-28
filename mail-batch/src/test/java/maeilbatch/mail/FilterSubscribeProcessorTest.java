package maeilbatch.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FilterSubscribeProcessorTest {

    @Test
    @DisplayName("탈퇴하지 않은 구독자는 그대로 통과시킨다.")
    void processActiveSubscribe() {
        FilterSubscribeProcessor processor = new FilterSubscribeProcessor();
        Subscribe subscribe = createSubscribe();

        Subscribe result = processor.process(subscribe);

        assertThat(result).isSameAs(subscribe);
    }

    @Test
    @DisplayName("탈퇴한 구독자는 필터링한다.")
    void processDeletedSubscribe() {
        FilterSubscribeProcessor processor = new FilterSubscribeProcessor();
        Subscribe subscribe = createUnsubscribedSubscribe();

        Subscribe result = processor.process(subscribe);

        assertAll(
                () -> assertThat(subscribe.getDeletedAt()).isNotNull(),
                () -> assertThat(result).isNull()
        );
    }

    private Subscribe createSubscribe() {
        return new Subscribe("test@test.com", QuestionCategory.BACKEND, SubscribeFrequency.DAILY);
    }

    private Subscribe createUnsubscribedSubscribe() {
        Subscribe subscribe = createSubscribe();
        subscribe.unsubscribe();

        return subscribe;
    }
}
