package maeilmail.subscribequestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import maeilmail.PaginationResponse;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionRepository;
import maeilmail.subscribe.Subscribe;
import maeilmail.subscribe.SubscribeRepository;
import maeilmail.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

class SubscribeQuestionQueryServiceTest extends IntegrationTestSupport {

    @Autowired
    private SubscribeQuestionQueryService subscribeQuestionQueryService;

    @Autowired
    private SubscribeQuestionRepository subscribeQuestionRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @BeforeEach
    void setUp() {
        // subscribers
        Subscribe subscribe1 = new Subscribe("111@gmail.com", QuestionCategory.BACKEND);
        Subscribe subscribe2 = new Subscribe("222@gmail.com", QuestionCategory.BACKEND);
        Subscribe subscribe3 = new Subscribe("333@gmail.com", QuestionCategory.FRONTEND);
        subscribeRepository.saveAll(List.of(subscribe1, subscribe2, subscribe3));

        // questions
        Question question1 = new Question("title-1", "cotent-1", QuestionCategory.BACKEND);
        Question question2 = new Question("title-2", "cotent-2", QuestionCategory.BACKEND);
        Question question3 = new Question("title-3", "cotent-3", QuestionCategory.BACKEND);
        Question question4 = new Question("title-4", "cotent-4", QuestionCategory.FRONTEND);
        Question question5 = new Question("title-5", "cotent-5", QuestionCategory.FRONTEND);
        questionRepository.saveAll(List.of(question1, question2, question3, question4, question5));

        // target subscriber's subscribeQuestions
        SubscribeQuestion subscribeQuestion1 = new SubscribeQuestion(subscribe1, question1, true);
        SubscribeQuestion subscribeQuestion2 = new SubscribeQuestion(subscribe1, question2, true);
        SubscribeQuestion subscribeQuestion3 = new SubscribeQuestion(subscribe1, question4, true);

        // other subscriber's
        SubscribeQuestion subscribeQuestion4 = new SubscribeQuestion(subscribe2, question1, true);
        SubscribeQuestion subscribeQuestion5 = new SubscribeQuestion(subscribe2, question4, true);
        SubscribeQuestion subscribeQuestion6 = new SubscribeQuestion(subscribe2, question2, true);
        SubscribeQuestion subscribeQuestion7 = new SubscribeQuestion(subscribe2, question5, true);
        subscribeQuestionRepository.saveAll(
                List.of(subscribeQuestion1,
                        subscribeQuestion2,
                        subscribeQuestion3,
                        subscribeQuestion4,
                        subscribeQuestion5,
                        subscribeQuestion6,
                        subscribeQuestion7)
        );
    }

    @DisplayName("구독자의 이메일과 카테고리에 따라 여태까지 받은 모든 질문지를 조회한다.")
    @Test
    void pageByEmailAndCategory() {
        PaginationResponse<SubscribeQuestionSummary> response =
                subscribeQuestionQueryService.pageByEmailAndCategory(
                        "111@gmail.com",
                        "backend",
                        PageRequest.of(0, 10)
                );

        assertAll(
                () -> assertThat(response.isLastPage()).isTrue(),
                () -> assertThat(response.data()).hasSize(2),
                () -> assertThat(response.totalPage()).isEqualTo(1),
                () -> assertThat(response.data())
                        .map(SubscribeQuestionSummary::title)
                        .containsExactlyElementsOf(List.of("title-1", "title-2"))
        );
    }

    @DisplayName("카테고리가 all 이면 구독자가 받은 모든 카테고리의 질문을 조회한다.")
    @Test
    void pageByEmailAndDefaultCategory() {
        PaginationResponse<SubscribeQuestionSummary> response =
                subscribeQuestionQueryService.pageByEmailAndCategory(
                        "111@gmail.com",
                        "all",
                        PageRequest.of(0, 10)
                );

        assertAll(
                () -> assertThat(response.isLastPage()).isTrue(),
                () -> assertThat(response.data()).hasSize(3),
                () -> assertThat(response.totalPage()).isEqualTo(1),
                () -> assertThat(response.data())
                        .map(SubscribeQuestionSummary::title)
                        .containsExactlyElementsOf(List.of("title-1", "title-2", "title-4"))
        );
    }
}
