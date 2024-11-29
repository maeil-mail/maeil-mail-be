package maeilmail.subscribe;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import maeilmail.question.QuestionCategory;
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
        SubscribeRequest request = createRequest(List.of("backend"));

        subscribeService.subscribe(request);

        List<Subscribe> result = subscribeRepository.findAll();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("신규 구독자가 두 개의 카테고리를 구독한다.")
    void subscribes() {
        SubscribeRequest request = createRequest(List.of("backend", "frontend"));

        subscribeService.subscribe(request);

        List<Subscribe> result = subscribeRepository.findAll();
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("이미 존재하는 카테고리라면 신규로 구독할 수 없다.")
    void duplicate() {
        SubscribeRequest request = createRequest(List.of("backend", "frontend"));
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
        SubscribeRequest request = createRequest(List.of("backend"));
        subscribeService.subscribe(request);

        SubscribeRequest secondRequest = createRequest(List.of("backend", "frontend"));
        subscribeService.subscribe(secondRequest);

        List<Subscribe> result = subscribeRepository.findAll();
        assertThat(result)
                .map(Subscribe::getCategory)
                .containsExactly(QuestionCategory.BACKEND, QuestionCategory.FRONTEND);
    }

    private SubscribeRequest createRequest(List<String> category) {
        return new SubscribeRequest("test@gmail.com", category, "1234", "daily");
    }
}
