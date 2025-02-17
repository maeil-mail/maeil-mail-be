package maeilmail.support.data;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.EntityManager;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionRepository;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import maeilmail.subscribe.command.domain.SubscribeQuestionRepository;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import maeilmail.support.IntegrationTestSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 12월 30일 월요일인 경우 :
 * - 기대 전송 메일 건수 : 9건
 * - 실제 전송 메일 건수 : 9건
 * - 성공 건수 : 7건
 * - 실패 건수 : 2건
 * 12월 31일 화요일인 경우 :
 * - 기대 전송 질문 건수 : 9건
 * - 실제 전송 질문 건수 : 9건
 * - 성공 건수 : 9건
 * - 실패 건수 : 0건
 */
@Component
public class SendReportCountingCase extends IntegrationTestSupport {

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SubscribeQuestionRepository subscribeQuestionRepository;

    @Autowired
    private EntityManager entityManager;

    public void createData() {
        setJpaAuditingTime(LocalDate.of(2024, 11, 1).atStartOfDay());
        Subscribe subscribe = createSubscribe(SubscribeFrequency.WEEKLY);
        Subscribe acceptedMailButUnsubscribed = createSubscribe(SubscribeFrequency.DAILY);
        Question question = createQuestion();
        for (int i = 0; i < 7; i++) {
            createSubscribe(SubscribeFrequency.DAILY);
        }

        // 구독 해지자 30일, 31일 모두 카운팅 대상  x
        Subscribe unsubscribe = createSubscribe(SubscribeFrequency.DAILY);
        LocalDateTime deletedAt1 = LocalDate.of(2024, 11, 2).atStartOfDay();
        unsubscribe(unsubscribe, deletedAt1);

        // 구독 해지자지만, 메일 전송 대상이었으므로 카운팅 대상(30일날만) o
        LocalDateTime deletedAt2 = LocalDateTime.of(2024, 12, 30, 7, 23, 0);
        unsubscribe(acceptedMailButUnsubscribed, deletedAt2);

        // 30일 7시 가입자이므로 30일날은 카운팅 대상 x
        setJpaAuditingTime(LocalDateTime.of(2024, 12, 30, 7, 0, 0));
        createSubscribe(SubscribeFrequency.DAILY);

        LocalDateTime monday = LocalDateTime.of(2024, 12, 30, 7, 10, 0);
        createSubscribeQuestions(subscribe, question, monday, 7, 2);

        LocalDateTime tuesday = LocalDateTime.of(2024, 12, 31, 7, 10, 0);
        createSubscribeQuestions(subscribe, question, tuesday, 9, 0);

        entityManager.flush();
    }

    private void unsubscribe(Subscribe subscribe, LocalDateTime dateTime) {
        try {
            Field deletedAt = subscribe.getClass().getDeclaredField("deletedAt");
            deletedAt.setAccessible(true);
            deletedAt.set(subscribe, dateTime);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createSubscribeQuestions(Subscribe subscribe, Question question, LocalDateTime time, int success, int fail) {
        setJpaAuditingTime(time);
        for (int i = 0; i < success; i++) {
            createSubscribeQuestion(true, subscribe, question);
        }
        for (int i = 0; i < fail; i++) {
            createSubscribeQuestion(false, subscribe, question);
        }
    }

    private Subscribe createSubscribe(SubscribeFrequency subscribeFrequency) {
        Subscribe subscribe = new Subscribe("email@test.com", QuestionCategory.BACKEND, subscribeFrequency);

        return subscribeRepository.save(subscribe);
    }

    private void createSubscribeQuestion(boolean isSuccess, Subscribe subscribe, Question question) {
        subscribeQuestionRepository.save(new SubscribeQuestion(subscribe, question, isSuccess));
    }

    private Question createQuestion() {
        return questionRepository.save(
                new Question(
                        "test-title",
                        "test-content",
                        QuestionCategory.BACKEND
                )
        );
    }
}
