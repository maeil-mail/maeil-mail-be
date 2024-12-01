package maeilmail.subscribe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionRepository;
import maeilmail.subscribequestion.SubscribeQuestion;
import maeilmail.subscribequestion.SubscribeQuestionRepository;
import maeilmail.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SubscribeRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SubscribeQuestionRepository subscribeQuestionRepository;

    @Test
    @DisplayName("구독자의 질문지 시퀀스를 증가시킨다.")
    void increaseNextQuestionSequence() {
        LocalDateTime baseDateTime = LocalDateTime.of(2024, 11, 7, 7, 0);
        LocalDateTime expectedChangeTime = baseDateTime.minusSeconds(1);
        createSubscribe("test1@test.com", QuestionCategory.BACKEND, expectedChangeTime);
        createSubscribe("test2@test.com", QuestionCategory.FRONTEND, expectedChangeTime);
        createSubscribe("test3@test.com", QuestionCategory.BACKEND, baseDateTime);
        for (Subscribe subscribe : subscribeRepository.findAll()) {
            createSubscribeQuestion(subscribe, createQuestion(subscribe.getCategory()));
        }

        subscribeRepository.increaseNextQuestionSequence(baseDateTime);

        List<Subscribe> subscribes = subscribeRepository.findAll();
        assertThat(subscribes)
                .filteredOn("nextQuestionSequence", 1L)
                .map(Subscribe::getEmail)
                .containsExactlyInAnyOrder("test1@test.com", "test2@test.com");
    }

    @Test
    @DisplayName("해당 구독자가 금일 받은 질문의 수만큼 시퀀스를 증가시킨다.")
    void increaseNextQuestionSequenceWithOffset() {
        LocalDateTime baseDateTime = LocalDateTime.of(2024, 11, 7, 7, 0);
        LocalDateTime expectedChangeTime = baseDateTime.minusSeconds(1);
        createSubscribe("test1@test.com", QuestionCategory.BACKEND, expectedChangeTime);
        createSubscribe("test2@test.com", QuestionCategory.FRONTEND, expectedChangeTime);
        createSubscribe("test3@test.com", QuestionCategory.BACKEND, baseDateTime);

        /**
         * 테스트 가정)
         * 프론트엔드 구독자는 데일리라서 1을 기대한다.
         * 백엔드 구독자는 위클리라서 5를 기대한다.
         */
        int frontendCount = 1;
        for (Subscribe subscribe : subscribeRepository.findAll()) {
            if (subscribe.getCategory() == QuestionCategory.FRONTEND && frontendCount-- != 0) {
                createSubscribeQuestion(subscribe, createQuestion(subscribe.getCategory()));
            } else {
                createSubscribeQuestion(subscribe, createQuestion(subscribe.getCategory()));
                createSubscribeQuestion(subscribe, createQuestion(subscribe.getCategory()));
                createSubscribeQuestion(subscribe, createQuestion(subscribe.getCategory()));
                createSubscribeQuestion(subscribe, createQuestion(subscribe.getCategory()));
                createSubscribeQuestion(subscribe, createQuestion(subscribe.getCategory()));
            }
        }

        subscribeRepository.increaseNextQuestionSequence(baseDateTime);

        List<Subscribe> subscribes = subscribeRepository.findAll();
        Subscribe subscribe1 = subscribes.get(0); // 백엔드 유저, 질문 받음
        Subscribe subscribe2 = subscribes.get(1); // 프론트 유저, 질문 1개
        Subscribe subscribe3 = subscribes.get(2); // 백엔드 유저, 질문 못받은 케이스
        assertAll(
                () -> assertThat(subscribe1.getNextQuestionSequence()).isEqualTo(5L),
                () -> assertThat(subscribe2.getNextQuestionSequence()).isEqualTo(1L),
                () -> assertThat(subscribe3.getNextQuestionSequence()).isEqualTo(0L)
        );
    }

    private void createSubscribe(
            String email,
            QuestionCategory category,
            LocalDateTime createdAt
    ) {
        String sql = "insert into subscribe(email, category, next_question_sequence, created_at, token, frequency) values(?, ?, ?, ?, ?, ?);";
        Query nativeQuery = entityManager.createNativeQuery(sql);
        nativeQuery.setParameter(1, email);
        nativeQuery.setParameter(2, category.toLowerCase());
        nativeQuery.setParameter(3, 0);
        nativeQuery.setParameter(4, createdAt);
        nativeQuery.setParameter(5, UUID.randomUUID().toString());
        nativeQuery.setParameter(6, "daily");
        nativeQuery.executeUpdate();
    }

    private Question createQuestion(QuestionCategory category) {
        Question question = new Question("question", "content", category);

        return questionRepository.save(question);
    }

    private void createSubscribeQuestion(Subscribe subscribe, Question question) {
        SubscribeQuestion subscribeQuestion = SubscribeQuestion.success(subscribe, question);

        subscribeQuestionRepository.save(subscribeQuestion);
    }
}
