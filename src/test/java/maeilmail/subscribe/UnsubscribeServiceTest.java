package maeilmail.subscribe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;
import maeilmail.question.QuestionCategory;
import maeilmail.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UnsubscribeServiceTest extends IntegrationTestSupport {

    @Autowired
    private UnsubscribeService unsubscribeService;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Test
    @DisplayName("존재하지 않는 구독자를 해지할 수 없다.")
    void unknownSubscribe() {
        UnsubscribeRequest request = new UnsubscribeRequest("unknown@test.com", "unknown-token");

        assertThatThrownBy(() -> unsubscribeService.unsubscribe(request))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("구독을 해지할 수 있다.")
    void unsubscribe() {
        Subscribe subscribe = createSubscribe();
        UnsubscribeRequest request = new UnsubscribeRequest(subscribe.getEmail(), subscribe.getToken());

        unsubscribeService.unsubscribe(request);

        assertThat(subscribe.getDeletedAt()).isNotNull();

    }

    private Subscribe createSubscribe() {
        Subscribe subscribe = new Subscribe("email@test.com", QuestionCategory.BACKEND);

        return subscribeRepository.save(subscribe);
    }
}
