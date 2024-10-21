package maeilmail.subscribe.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;

import java.util.List;
import maeilmail.mail.MailSender;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.core.request.SubscribeQuestionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class SubscribeQuestionServiceTest {

    @Autowired
    private SubscribeQuestionService subscribeQuestionService;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @MockBean
    private MailSender mailSender;

    @MockBean
    private SubscribeVerifyService subscribeVerifyService;

    @Test
    @DisplayName("신규 구독자를 생성한다.")
    void subscribe() {
        willDoNothing()
                .given(mailSender)
                .sendMail(any());
        willDoNothing()
                .given(subscribeVerifyService)
                .verify(any(), any());
        SubscribeQuestionRequest request = createRequest(List.of("backend"));

        subscribeQuestionService.subscribe(request);

        List<Subscribe> result = subscribeRepository.findAll();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("신규 구독자가 두 개의 카테고리를 구독한다.")
    void subscribes() {
        willDoNothing()
                .given(mailSender)
                .sendMail(any());
        willDoNothing()
                .given(subscribeVerifyService)
                .verify(any(), any());
        SubscribeQuestionRequest request = createRequest(List.of("backend", "frontend"));

        subscribeQuestionService.subscribe(request);

        List<Subscribe> result = subscribeRepository.findAll();
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("이미 존재하는 카테고리라면 신규로 구독할 수 없다.")
    void duplicate() {
        willDoNothing()
                .given(mailSender)
                .sendMail(any());
        willDoNothing()
                .given(subscribeVerifyService)
                .verify(any(), any());
        SubscribeQuestionRequest request = createRequest(List.of("backend", "frontend"));
        subscribeQuestionService.subscribe(request);

        subscribeQuestionService.subscribe(request);

        List<Subscribe> result = subscribeRepository.findAll();

        assertThat(result)
                .map(Subscribe::getCategory)
                .containsExactly(QuestionCategory.BACKEND, QuestionCategory.FRONTEND);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리라면 신규로 구독할 수 있다.")
    void duplicate2() {
        willDoNothing()
                .given(mailSender)
                .sendMail(any());
        willDoNothing()
                .given(subscribeVerifyService)
                .verify(any(), any());
        SubscribeQuestionRequest request = createRequest(List.of("backend"));
        subscribeQuestionService.subscribe(request);

        SubscribeQuestionRequest secondRequest = createRequest(List.of("backend", "frontend"));
        subscribeQuestionService.subscribe(secondRequest);

        List<Subscribe> result = subscribeRepository.findAll();
        assertThat(result)
                .map(Subscribe::getCategory)
                .containsExactly(QuestionCategory.BACKEND, QuestionCategory.FRONTEND);
    }

    private SubscribeQuestionRequest createRequest(List<String> category) {
        return new SubscribeQuestionRequest("test@gmail.com", category, "1234");
    }
}
