package maeilmail.subscribe.command.domain;

import static maeilmail.subscribe.command.domain.SubscribeFrequency.DAILY;
import static maeilmail.subscribe.command.domain.SubscribeFrequency.WEEKLY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.util.List;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionRepository;
import maeilmail.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SubscribeRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SubscribeQuestionRepository subscribeQuestionRepository;

    @Test
    @DisplayName("구독자의 질문지 시퀀스를 증가시킨다.")
    void increaseNextQuestionSequence() {
        LocalDateTime baseDateTime = LocalDateTime.of(2024, 11, 7, 7, 0);
        LocalDateTime expectedChangeTime = baseDateTime.minusSeconds(1);
        createSubscribe("test1@test.com", expectedChangeTime, DAILY);
        createSubscribe("test2@test.com", expectedChangeTime, DAILY);
        createSubscribe("test3@test.com", baseDateTime, DAILY);
        setJpaAuditingTime(LocalDateTime.now());
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
        createSubscribe("test1@test.com", expectedChangeTime, WEEKLY);
        createSubscribe("test2@test.com", expectedChangeTime, DAILY);
        createSubscribe("test3@test.com", baseDateTime, DAILY);

        /**
         * 1. 주간 메일을 받은 케이스
         * 2. 일간 메일을 받은 케이스
         * 3. 메일을 받지 못한 케이스
         */
        setJpaAuditingTime(LocalDateTime.now());
        for (Subscribe subscribe : subscribeRepository.findAll()) {
            if (subscribe.getFrequency() == DAILY) {
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
        Subscribe subscribe1 = subscribes.get(0); // 주간 메일을 받은 케이스
        Subscribe subscribe2 = subscribes.get(1); // 일간 메일을 받은 케이스
        Subscribe subscribe3 = subscribes.get(2); // 메일을 받지 못한 케이스
        assertAll(
                () -> assertThat(subscribe1.getNextQuestionSequence()).isEqualTo(5L),
                () -> assertThat(subscribe2.getNextQuestionSequence()).isEqualTo(1L),
                () -> assertThat(subscribe3.getNextQuestionSequence()).isEqualTo(0L)
        );
    }

    private void createSubscribe(String email, LocalDateTime createdAt, SubscribeFrequency frequency) {
        setJpaAuditingTime(createdAt);
        Subscribe subscribe = new Subscribe(email, QuestionCategory.FRONTEND, frequency);

        subscribeRepository.save(subscribe);
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
