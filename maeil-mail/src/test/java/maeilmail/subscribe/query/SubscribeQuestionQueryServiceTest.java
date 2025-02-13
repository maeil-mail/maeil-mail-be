package maeilmail.subscribe.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionRepository;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import maeilmail.subscribe.command.domain.SubscribeQuestionRepository;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import maeilmail.support.IntegrationTestSupport;
import maeilsupport.PaginationResponse;
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

    @Test
    @DisplayName("구독자의 이메일과 카테고리에 따라 여태까지 받은 모든 질문지를 조회한다.")
    void pageByEmailAndCategory() {
        createData();

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
                        .containsExactlyElementsOf(List.of("title-2", "title-1"))
        );
    }

    @Test
    @DisplayName("카테고리가 all 이면 구독자가 받은 모든 카테고리의 질문을 조회한다.")
    void pageByEmailAndDefaultCategory() {
        createData();

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
                        .containsExactlyElementsOf(List.of("title-4", "title-2", "title-1"))
        );
    }

    @Test
    @DisplayName("주간 질문지를 조회한다.")
    void queryWeeklyQuestions() {
        Subscribe subscribe = new Subscribe("test@gmail.com", QuestionCategory.BACKEND, SubscribeFrequency.WEEKLY);
        subscribeRepository.save(subscribe);
        List<Question> questions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Question question = new Question("title", "content", QuestionCategory.BACKEND);
            questions.add(questionRepository.save(question));
        }

        // 2월 1주차 질문
        setJpaAuditingTime(LocalDateTime.of(2025, 2, 3, 7, 5, 0));
        for (int i = 0; i < 5; i++) {
            Question question = questions.get(i);
            SubscribeQuestion subscribeQuestion = new SubscribeQuestion(subscribe, question, true);
            subscribeQuestionRepository.save(subscribeQuestion);
        }

        // 2월 2주차 질문(조회 대상)
        setJpaAuditingTime(LocalDateTime.of(2025, 2, 10, 7, 5, 0));
        List<Long> expectedId = new ArrayList<>();
        for (int i = 5; i < questions.size(); i++) {
            Question question = questions.get(i);
            SubscribeQuestion subscribeQuestion = new SubscribeQuestion(subscribe, question, true);
            subscribeQuestionRepository.save(subscribeQuestion);
            expectedId.add(subscribeQuestion.getId());
        }

        WeeklySubscribeQuestionResponse response = subscribeQuestionQueryService
                .queryWeeklyQuestions(subscribe.getEmail(), "backend", 2025L, 2L, 2L);

        assertAll(
                () -> assertThat(response.weekLabel()).isEqualTo("2월 2주차"),
                () -> assertThat(response.questions())
                        .map(WeeklySubscribeQuestionSummary::getIndex)
                        .containsExactlyElementsOf(List.of(1L, 2L, 3L, 4L, 5L)),
                () -> assertThat(response.questions())
                        .map(WeeklySubscribeQuestionSummary::getId)
                        .containsExactlyElementsOf(expectedId)
        );
    }

    private void createData() {
        // subscribers
        Subscribe subscribe1 = new Subscribe("111@gmail.com", QuestionCategory.BACKEND, SubscribeFrequency.DAILY);
        Subscribe subscribe2 = new Subscribe("222@gmail.com", QuestionCategory.BACKEND, SubscribeFrequency.DAILY);
        Subscribe subscribe3 = new Subscribe("333@gmail.com", QuestionCategory.FRONTEND, SubscribeFrequency.DAILY);
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
}
