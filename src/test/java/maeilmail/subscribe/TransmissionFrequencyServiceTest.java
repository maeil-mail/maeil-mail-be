package maeilmail.subscribe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.NoSuchElementException;
import maeilmail.question.QuestionCategory;
import maeilmail.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TransmissionFrequencyServiceTest extends IntegrationTestSupport {

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private TransmissionFrequencyService transmissionFrequencyService;

    @Test
    @DisplayName("구독자의 주기를 변경한다.")
    void changeFrequency() {
        Subscribe subscribe = createSubscribe(QuestionCategory.BACKEND);
        TransmissionFrequencyRequest request = createRequest(subscribe.getToken(), subscribe.getEmail());

        transmissionFrequencyService.changeFrequency(request);

        assertThat(subscribe.getFrequency()).isEqualTo(SubscribeFrequency.WEEKLY);
    }

    @Test
    @DisplayName("이메일에 해당되는 모든 구독자의 주기를 전역적으로 설정한다.")
    void changeFrequencyTotal() {
        Subscribe subscribe1 = createSubscribe(QuestionCategory.BACKEND);
        Subscribe subscribe2 = createSubscribe(QuestionCategory.FRONTEND);
        TransmissionFrequencyRequest request = createRequest(subscribe1.getToken(), subscribe1.getEmail());

        transmissionFrequencyService.changeFrequency(request);

        assertThat(subscribe1.getFrequency()).isEqualTo(SubscribeFrequency.WEEKLY);
        assertThat(subscribe2.getFrequency()).isEqualTo(SubscribeFrequency.WEEKLY);
    }

    @Test
    @DisplayName("토큰이 적어도 하나의 구독자에 해당되어야 주기 변경이 가능하다.")
    void cantChange() {
        Subscribe subscribe1 = createSubscribe(QuestionCategory.BACKEND);
        Subscribe subscribe2 = createSubscribe(QuestionCategory.FRONTEND);
        TransmissionFrequencyRequest request = createRequest("unknown-token", subscribe1.getEmail());

        assertThatThrownBy(() -> transmissionFrequencyService.changeFrequency(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("자신의 이메일 전송 주기만 변경 가능합니다.");
    }

    @Test
    @DisplayName("존재하지 않는 이메일에 대한 주기는 변경이 불가하다.")
    void noneExist() {
        TransmissionFrequencyRequest request = createRequest("unknown-token", "unknown-email");

        assertThatThrownBy(() -> transmissionFrequencyService.changeFrequency(request))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("구독 해지된 구독자의 주기는 변경하지 않는다.")
    void cantChangeUnsubscribedSubscribe() {
        Subscribe subscribe1 = createSubscribe(QuestionCategory.BACKEND);
        Subscribe subscribe2 = createSubscribe(QuestionCategory.FRONTEND);
        subscribe1.unsubscribe();
        TransmissionFrequencyRequest request = createRequest(subscribe2.getToken(), subscribe2.getEmail());

        transmissionFrequencyService.changeFrequency(request);

        List<Subscribe> result = subscribeRepository.findAll();
        assertThat(result)
                .map(Subscribe::getFrequency)
                .containsExactlyInAnyOrder(SubscribeFrequency.DAILY, SubscribeFrequency.WEEKLY);
    }

    @Test
    @DisplayName("구독자의 전송 주기를 조회한다.")
    void getFrequency() {
        Subscribe subscribe = createSubscribe(QuestionCategory.BACKEND);

        TransmissionFrequencyResponse result = transmissionFrequencyService.getFrequency(subscribe.getEmail());

        assertThat(result.frequency()).isEqualTo(SubscribeFrequency.DAILY.toLowerCase());
    }

    private TransmissionFrequencyRequest createRequest(String token, String email) {
        return new TransmissionFrequencyRequest(email, token, "weekly");
    }

    private Subscribe createSubscribe(QuestionCategory category) {
        Subscribe subscribe = new Subscribe("email@test.com", category, SubscribeFrequency.DAILY);

        return subscribeRepository.save(subscribe);
    }
}
