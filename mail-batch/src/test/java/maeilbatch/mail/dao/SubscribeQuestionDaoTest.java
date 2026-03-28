package maeilbatch.mail.dao;

import static maeilmail.subscribe.command.domain.SubscribeFrequency.DAILY;
import static maeilmail.subscribe.command.domain.SubscribeFrequency.WEEKLY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.util.List;
import maeilbatch.support.IntegrationTestSupport;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionRepository;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import maeilmail.subscribe.command.domain.SubscribeQuestionRepository;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SubscribeQuestionDaoTest extends IntegrationTestSupport {

    @Autowired
    private SubscribeQuestionDao subscribeQuestionDao;

    @Autowired
    private SubscribeQuestionRepository subscribeQuestionRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @AfterEach
    void tearDown() {
        subscribeQuestionRepository.deleteAll();
        subscribeRepository.deleteAll();
        questionRepository.deleteAll();
    }

    @Test
    @DisplayName("전달한 subscribe/question 키 목록으로 id를 조회한다.")
    void findIdsByKeys() {
        Subscribe subscribe1 = createSubscribe("one@test.com", SubscribeFrequency.DAILY);
        Subscribe subscribe2 = createSubscribe("two@test.com", SubscribeFrequency.WEEKLY);
        Question question1 = createQuestion("question-1");
        Question question2 = createQuestion("question-2");
        Question question3 = createQuestion("question-3");
        SubscribeQuestion saved1 = subscribeQuestionRepository.save(SubscribeQuestion.success(subscribe1, question1));
        SubscribeQuestion saved2 = subscribeQuestionRepository.save(SubscribeQuestion.success(subscribe2, question2));
        subscribeQuestionRepository.save(SubscribeQuestion.success(subscribe2, question3));

        List<Long> result = subscribeQuestionDao.findIdsByKeys(List.of(
                new SubscribeQuestionKey(subscribe1.getId(), question1.getId()),
                new SubscribeQuestionKey(subscribe2.getId(), question2.getId())
        ));

        assertThat(result).containsExactlyInAnyOrder(saved1.getId(), saved2.getId());
    }

    @Test
    @DisplayName("빈 키 목록으로 id 조회를 요청하면 빈 목록을 반환한다.")
    void findIdsByKeysEmptyKeys() {
        List<Long> result = subscribeQuestionDao.findIdsByKeys(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("전달한 subscribeQuestion 목록을 일괄 저장한다.")
    void batchInsert() {
        Subscribe subscribe1 = createSubscribe("one@test.com", SubscribeFrequency.DAILY);
        Subscribe subscribe2 = createSubscribe("two@test.com", SubscribeFrequency.WEEKLY);
        Question question1 = createQuestion("question-1");
        Question question2 = createQuestion("question-2");
        SubscribeQuestion sq1 = SubscribeQuestion.success(subscribe1, question1);
        SubscribeQuestion sq2 = SubscribeQuestion.fail(subscribe2, question2);

        subscribeQuestionDao.batchInsert(List.of(sq1, sq2));

        List<SubscribeQuestion> result = subscribeQuestionRepository.findAll();
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result)
                        .extracting(it -> it.getSubscribe().getId())
                        .containsExactlyInAnyOrder(subscribe1.getId(), subscribe2.getId()),
                () -> assertThat(result)
                        .extracting(it -> it.getQuestion().getId())
                        .containsExactlyInAnyOrder(question1.getId(), question2.getId()),
                () -> assertThat(result)
                        .extracting(SubscribeQuestion::isSuccess)
                        .containsExactlyInAnyOrder(true, false)
        );
    }

    @Test
    @DisplayName("빈 subscribeQuestion 목록으로 일괄 저장을 요청하면 아무 것도 저장하지 않는다.")
    void batchInsertEmptyQuestionsNoOp() {
        subscribeQuestionDao.batchInsert(List.of());

        assertThat(subscribeQuestionRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("전달한 id 목록으로 subscribeQuestion을 일괄 삭제한다.")
    void deleteByIds() {
        Subscribe subscribe = createSubscribe("one@test.com", SubscribeFrequency.DAILY);
        Question question1 = createQuestion("question-1");
        Question question2 = createQuestion("question-2");
        Question question3 = createQuestion("question-3");
        SubscribeQuestion saved1 = subscribeQuestionRepository.save(SubscribeQuestion.success(subscribe, question1));
        SubscribeQuestion saved2 = subscribeQuestionRepository.save(SubscribeQuestion.success(subscribe, question2));
        SubscribeQuestion keep = subscribeQuestionRepository.save(SubscribeQuestion.success(subscribe, question3));

        subscribeQuestionDao.deleteByIds(List.of(saved1.getId(), saved2.getId()));

        List<SubscribeQuestion> result = subscribeQuestionRepository.findAll();
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.get(0).getId()).isEqualTo(keep.getId())
        );
    }

    @Test
    @DisplayName("빈 id 목록으로 일괄 삭제를 요청하면 아무 것도 삭제하지 않는다.")
    void deleteByIdsEmptyIdsNoOp() {
        Subscribe subscribe = createSubscribe("one@test.com", SubscribeFrequency.DAILY);
        Question question = createQuestion("question-1");
        subscribeQuestionRepository.save(SubscribeQuestion.success(subscribe, question));

        subscribeQuestionDao.deleteByIds(List.of());

        assertThat(subscribeQuestionRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("구독자의 질문 개수만큼 시퀀스를 증가시킨다.")
    void increaseNextQuestionSequence() {
        LocalDateTime baseDateTime = LocalDateTime.of(2024, 11, 7, 7, 0, 0);
        LocalDateTime noneChange = baseDateTime.plusSeconds(1);
        createSubscribe("test1@test.com", baseDateTime, DAILY);
        createSubscribe("test2@test.com", baseDateTime, DAILY);
        createSubscribe("test3@test.com", noneChange, DAILY);
        setAuditingTime(LocalDateTime.now());
        for (Subscribe subscribe : subscribeRepository.findAll()) {
            createSubscribeQuestion(subscribe, createQuestion(subscribe.getCategory()));
        }

        subscribeQuestionDao.increaseNextQuestionSequence(baseDateTime);

        List<Subscribe> subscribes = subscribeRepository.findAll();

        assertThat(subscribes)
                .filteredOn("nextQuestionSequence", 1L)
                .map(Subscribe::getEmail)
                .containsExactlyInAnyOrder("test1@test.com", "test2@test.com", "test3@test.com");
    }

    @Test
    @DisplayName("해당 구독자가 받은 질문의 수만큼 시퀀스를 증가시킨다.")
    void increaseNextQuestionSequenceWithOffset() {
        LocalDateTime baseDateTime = LocalDateTime.of(2024, 11, 7, 7, 0, 1);
        LocalDateTime noneChange = baseDateTime.plusSeconds(1);
        createSubscribe("test1@test.com", baseDateTime, WEEKLY);
        createSubscribe("test2@test.com", baseDateTime, DAILY);
        createSubscribe("test3@test.com", noneChange, DAILY);
        setAuditingTime(LocalDateTime.now());
        for (Subscribe subscribe : subscribeRepository.findAll()) {
            if (subscribe.getFrequency() == DAILY) {
                createSubscribeQuestion(subscribe, createQuestion(subscribe.getCategory()));
                continue;
            }

            for (int i = 0; i < 5; i++) {
                createSubscribeQuestion(subscribe, createQuestion(subscribe.getCategory()));
            }
        }

        subscribeQuestionDao.increaseNextQuestionSequence(baseDateTime);

        List<Subscribe> subscribes = subscribeRepository.findAll();
        Subscribe subscribe1 = subscribes.get(0);
        Subscribe subscribe2 = subscribes.get(1);
        Subscribe subscribe3 = subscribes.get(2);

        assertAll(
                () -> assertThat(subscribe1.getNextQuestionSequence()).isEqualTo(5L),
                () -> assertThat(subscribe2.getNextQuestionSequence()).isEqualTo(1L),
                () -> assertThat(subscribe3.getNextQuestionSequence()).isEqualTo(1L)
        );
    }

    @Test
    @DisplayName("구독 해지자는 시퀀스를 증가시키지 않는다.")
    void increaseNextQuestionSequenceIgnoreUnsubscribed() {
        LocalDateTime baseDateTime = LocalDateTime.of(2024, 11, 7, 7, 0, 0);
        Subscribe active = createSubscribe("active@test.com", baseDateTime, DAILY);
        Subscribe unsubscribed = createSubscribe("unsubscribed@test.com", baseDateTime, DAILY);
        unsubscribed.unsubscribe();
        subscribeRepository.save(unsubscribed);

        setAuditingTime(LocalDateTime.now());
        createSubscribeQuestion(active, createQuestion(active.getCategory()));
        createSubscribeQuestion(unsubscribed, createQuestion(unsubscribed.getCategory()));

        subscribeQuestionDao.increaseNextQuestionSequence(baseDateTime);

        List<Subscribe> subscribes = subscribeRepository.findAll();
        Subscribe savedActive = subscribes.stream()
                .filter(subscribe -> subscribe.getEmail().equals("active@test.com"))
                .findFirst()
                .orElseThrow();
        Subscribe savedUnsubscribed = subscribes.stream()
                .filter(subscribe -> subscribe.getEmail().equals("unsubscribed@test.com"))
                .findFirst()
                .orElseThrow();

        assertAll(
                () -> assertThat(savedActive.getNextQuestionSequence()).isEqualTo(1L),
                () -> assertThat(savedUnsubscribed.getNextQuestionSequence()).isEqualTo(0L),
                () -> assertThat(savedUnsubscribed.getDeletedAt()).isNotNull()
        );
    }

    private Subscribe createSubscribe(String email, SubscribeFrequency frequency) {
        Subscribe subscribe = new Subscribe(email, QuestionCategory.BACKEND, frequency);
        return subscribeRepository.save(subscribe);
    }

    private Question createQuestion(String title) {
        Question question = new Question(title, "content-%s".formatted(title), QuestionCategory.BACKEND);
        return questionRepository.save(question);
    }

    private Question createQuestion(QuestionCategory category) {
        Question question = new Question("question", "content", category);

        return questionRepository.save(question);
    }

    private Subscribe createSubscribe(String email, LocalDateTime createdAt, SubscribeFrequency frequency) {
        setAuditingTime(createdAt);
        Subscribe subscribe = new Subscribe(email, QuestionCategory.FRONTEND, frequency);

        return subscribeRepository.save(subscribe);
    }

    private void createSubscribeQuestion(Subscribe subscribe, Question question) {
        SubscribeQuestion subscribeQuestion = SubscribeQuestion.success(subscribe, question);

        subscribeQuestionRepository.save(subscribeQuestion);
    }
}
