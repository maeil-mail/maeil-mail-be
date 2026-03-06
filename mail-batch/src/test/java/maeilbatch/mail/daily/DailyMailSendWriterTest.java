package maeilbatch.mail.daily;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.util.List;
import maeilbatch.forward.ForwardLog;
import maeilbatch.forward.ForwardRepository;
import maeilbatch.forward.ForwardStatus;
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
import org.springframework.batch.item.Chunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class DailyMailSendWriterTest extends IntegrationTestSupport {

    private static final String SUBSCRIBE_EMAIL = "daily@test.com";
    private static final String QUESTION_TITLE = "title";
    private static final String MESSAGE_SUBJECT = "subject";
    private static final String MESSAGE_TEXT = "text";

    @Autowired
    private DailyMailSendWriter writer;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SubscribeQuestionRepository subscribeQuestionRepository;

    @Autowired
    private ForwardRepository forwardRepository;

    @AfterEach
    void tearDown() {
        forwardRepository.deleteAll();
        subscribeQuestionRepository.deleteAll();
        questionRepository.deleteAll();
        subscribeRepository.deleteAll();
    }

    @Test
    @DisplayName("전송 이력을 저장하고 forward 로그를 만든다.")
    void write() {
        Subscribe subscribe = createSubscribe();
        Question question = createQuestion();
        DailyMailPayload message = createMessage(subscribe, question);

        writer.write(new Chunk<>(List.of(message)));

        List<ForwardLog> forwardLogs = forwardRepository.findAll();

        assertAll(
                () -> assertThat(forwardLogs).hasSize(1),
                () -> assertThat(forwardLogs.get(0).getTo()).isEqualTo(SUBSCRIBE_EMAIL),
                () -> assertThat(forwardLogs.get(0).getSubject()).isEqualTo(MESSAGE_SUBJECT),
                () -> assertThat(forwardLogs.get(0).getText()).isEqualTo(MESSAGE_TEXT),
                () -> assertThat(forwardLogs.get(0).getStatus()).isEqualTo(ForwardStatus.PENDING)
        );
    }

    @Test
    @DisplayName("이미 전송된 질문지라면 기존 이력을 지우고 최신 이력으로 교체한다.")
    void writeReplaceAlreadySentHistory() {
        Subscribe subscribe = createSubscribe();
        Question question = createQuestion();
        setJpaAuditingTime(LocalDateTime.of(2025, 5, 1, 7, 0));
        createSubscribeQuestion(subscribe, question);
        setJpaAuditingTime(LocalDateTime.of(2025, 5, 2, 7, 0));
        DailyMailPayload message = createMessage(subscribe, question);

        writer.write(new Chunk<>(List.of(message)));

        List<SubscribeQuestion> subscribeQuestions = subscribeQuestionRepository.findAll();
        assertAll(
                () -> assertThat(subscribeQuestions).hasSize(1),
                () -> assertThat(subscribeQuestions.get(0).isSuccess()).isTrue(),
                () -> assertThat(subscribeQuestions.get(0).getCreatedAt())
                        .isEqualTo(LocalDateTime.of(2025, 5, 2, 7, 0))
        );
    }

    private Subscribe createSubscribe() {
        return subscribeRepository.save(
                new Subscribe(SUBSCRIBE_EMAIL, QuestionCategory.BACKEND, SubscribeFrequency.DAILY)
        );
    }

    private Question createQuestion() {
        return questionRepository.save(new Question(QUESTION_TITLE, "content", QuestionCategory.BACKEND));
    }

    private void createSubscribeQuestion(Subscribe subscribe, Question question) {
        subscribeQuestionRepository.save(SubscribeQuestion.success(subscribe, question));
    }

    private DailyMailPayload createMessage(Subscribe subscribe, Question question) {
        return new DailyMailPayload(subscribe, question, MESSAGE_SUBJECT, MESSAGE_TEXT);
    }
}
