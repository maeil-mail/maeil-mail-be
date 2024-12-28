package maeilmail.subscribe.command.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.command.application.request.SubscribeRequest;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import maeilmail.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SubscribeServiceTest extends IntegrationTestSupport {

    @Autowired
    private SubscribeService subscribeService;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Test
    @DisplayName("신규 구독자를 생성한다.")
    void subscribe() {
        SubscribeRequest request = createRequest(List.of("backend"), "daily");

        subscribeService.subscribe(request);

        List<Subscribe> result = subscribeRepository.findAll();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("신규 구독자가 두 개의 카테고리를 구독한다.")
    void subscribes() {
        SubscribeRequest request = createRequest(List.of("backend", "frontend"), "daily");

        subscribeService.subscribe(request);

        List<Subscribe> result = subscribeRepository.findAll();
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("이미 존재하는 카테고리라면 신규로 구독할 수 없다.")
    void duplicate() {
        SubscribeRequest request = createRequest(List.of("backend", "frontend"), "daily");
        subscribeService.subscribe(request);

        subscribeService.subscribe(request);

        List<Subscribe> result = subscribeRepository.findAll();

        assertThat(result)
                .map(Subscribe::getCategory)
                .containsExactly(QuestionCategory.BACKEND, QuestionCategory.FRONTEND);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리라면 신규로 구독할 수 있다.")
    void duplicate2() {
        SubscribeRequest request = createRequest(List.of("backend"), "daily");
        subscribeService.subscribe(request);

        SubscribeRequest secondRequest = createRequest(List.of("backend", "frontend"), "daily");
        subscribeService.subscribe(secondRequest);

        List<Subscribe> result = subscribeRepository.findAll();
        assertThat(result)
                .map(Subscribe::getCategory)
                .containsExactly(QuestionCategory.BACKEND, QuestionCategory.FRONTEND);
    }

    @Test
    @DisplayName("질문 전송 주기를 주간으로 설정하여 구독할 수 있다.")
    void subscribeWithFrequency() {
        SubscribeRequest request = createRequest(List.of("backend"), "weekly");
        subscribeService.subscribe(request);

        List<Subscribe> result = subscribeRepository.findAll();
        assertThat(result)
                .map(Subscribe::getFrequency)
                .containsExactly(SubscribeFrequency.WEEKLY);
    }

    /**
     * ex)
     * 1. 오전 8시 전송 주기 weekly, frontend 구독
     * 2. 오전 9시 전송 주기 daily, backend 구독
     *
     * 해당 케이스에서 frontend, backend 구독 각각 다른 전송 주기를 가지게 되므로, 마지막 구독을 기준으로 전역 설정합니다.
     */
    @Test
    @DisplayName("같은 이메일로 등록된 구독이 이미 존재하는데, 카테고리가 다르다면 가장 마지막 전송 주기를 전역으로 적용한다.")
    void overrideFrequency() {
        SubscribeRequest weeklyRequest = createRequest(List.of("frontend"), "weekly");
        SubscribeRequest dailyRequest = createRequest(List.of("backend"), "daily");
        subscribeService.subscribe(weeklyRequest);
        subscribeService.subscribe(dailyRequest);

        List<Subscribe> result = subscribeRepository.findAll();
        assertThat(result)
                .map(Subscribe::getFrequency)
                .containsExactly(SubscribeFrequency.DAILY, SubscribeFrequency.DAILY);
    }

    private SubscribeRequest createRequest(List<String> category, String frequency) {
        return new SubscribeRequest("test@gmail.com", category, "1234", frequency);
    }
}
