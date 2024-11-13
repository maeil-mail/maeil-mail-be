package maeilmail.subscribe;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import maeilmail.question.QuestionCategory;
import maeilmail.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SubscribeRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("구독자의 질문지 시퀀스를 증가시킨다.")
    void increaseNextQuestionSequence() {
        LocalDateTime baseDateTime = LocalDateTime.of(2024, 11, 7, 7, 0);
        LocalDateTime expectedChangeTime = baseDateTime.minusSeconds(1);
        createSubscribe("test1@test.com", QuestionCategory.BACKEND, expectedChangeTime);
        createSubscribe("test2@test.com", QuestionCategory.FRONTEND, expectedChangeTime);
        createSubscribe("test3@test.com", QuestionCategory.BACKEND, baseDateTime);

        subscribeRepository.increaseNextQuestionSequence(baseDateTime);

        List<Subscribe> subscribes = subscribeRepository.findAll();
        assertThat(subscribes)
                .filteredOn("nextQuestionSequence", 1L)
                .map(Subscribe::getEmail)
                .containsExactlyInAnyOrder("test1@test.com", "test2@test.com");
    }

    private void createSubscribe(
            String email,
            QuestionCategory category,
            LocalDateTime createdAt
    ) {
        String sql = "insert into subscribe(email, category, next_question_sequence, created_at) values(?, ?, ?, ?);";
        Query nativeQuery = entityManager.createNativeQuery(sql);
        nativeQuery.setParameter(1, email);
        nativeQuery.setParameter(2, category.toLowerCase());
        nativeQuery.setParameter(3, 0);
        nativeQuery.setParameter(4, createdAt);
        nativeQuery.executeUpdate();
    }
}
