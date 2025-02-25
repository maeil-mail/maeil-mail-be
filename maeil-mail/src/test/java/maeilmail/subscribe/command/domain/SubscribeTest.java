package maeilmail.subscribe.command.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import maeilmail.question.QuestionCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SubscribeTest {

    @Test
    @DisplayName("구독을 취소할 수 있다.")
    void unsubscribe() {
        Subscribe subscribe = createSubscribe();

        subscribe.unsubscribe();

        assertThat(subscribe.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("구독 취소된 사용자는 다시 구독을 취소할 수 없다.")
    void alreadyUnsubscribed() {
        Subscribe subscribe = createSubscribe();
        subscribe.unsubscribe();

        assertThatThrownBy(subscribe::unsubscribe)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 구독이 취소되었습니다.");
    }

    private Subscribe createSubscribe() {
        return new Subscribe("atom@test.com", QuestionCategory.BACKEND, SubscribeFrequency.DAILY);
    }
}
