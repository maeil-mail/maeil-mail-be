package maeilbatch.mail.weekly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.ArrayList;
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
class WeeklyMailSendWriterTest extends IntegrationTestSupport {

    private static final String SUBSCRIBE_EMAIL = "weekly@test.com";
    private static final String MESSAGE_SUBJECT = "subject";
    private static final String MESSAGE_TEXT = "text";
    private static final int WEEKLY_SEND_COUNT = SubscribeFrequency.WEEKLY.getSendCount();

    @Autowired
    private WeeklyMailSendWriter writer;

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
    @DisplayName("weekly writer는 전송 이력을 롤링 저장하고 forward 로그를 만든다.")
    void write() {
        Subscribe subscribe = createSubscribe();
        List<Question> questions = createQuestions(WEEKLY_SEND_COUNT);
        createSentHistory(subscribe, questions.get(0));
        WeeklyMailMessage message = createMessage(subscribe, questions);

        writer.write(new Chunk<>(List.of(message)));

        List<SubscribeQuestion> subscribeQuestions = subscribeQuestionRepository.findAll();
        List<ForwardLog> forwardLogs = forwardRepository.findAll();

        assertAll(
                () -> assertThat(subscribeQuestions).hasSize(WEEKLY_SEND_COUNT),
                () -> assertThat(subscribeQuestions).allMatch(SubscribeQuestion::isSuccess),
                () -> assertThat(forwardLogs).hasSize(1),
                () -> assertThat(forwardLogs.get(0).getTo()).isEqualTo(SUBSCRIBE_EMAIL),
                () -> assertThat(forwardLogs.get(0).getSubject()).isEqualTo(MESSAGE_SUBJECT),
                () -> assertThat(forwardLogs.get(0).getText()).isEqualTo(MESSAGE_TEXT),
                () -> assertThat(forwardLogs.get(0).getStatus()).isEqualTo(ForwardStatus.PENDING)
        );
    }

    private Subscribe createSubscribe() {
        return subscribeRepository.save(
                new Subscribe(SUBSCRIBE_EMAIL, QuestionCategory.BACKEND, SubscribeFrequency.WEEKLY)
        );
    }

    private List<Question> createQuestions(int size) {
        List<Question> questions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            questions.add(new Question("title-" + i, "content-" + i, QuestionCategory.BACKEND));
        }

        return questionRepository.saveAll(questions);
    }

    private void createSentHistory(Subscribe subscribe, Question question) {
        subscribeQuestionRepository.save(SubscribeQuestion.success(subscribe, question));
    }

    private WeeklyMailMessage createMessage(Subscribe subscribe, List<Question> questions) {
        return new WeeklyMailMessage(subscribe, questions, MESSAGE_SUBJECT, MESSAGE_TEXT);
    }
}
