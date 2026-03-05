package maeilbatch.mail.daily;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

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
    @DisplayName("daily writer는 전송 이력을 롤링 저장하고 forward 로그를 만든다.")
    void write() {
        Subscribe subscribe = subscribeRepository.save(
                new Subscribe("daily@test.com", QuestionCategory.BACKEND, SubscribeFrequency.DAILY)
        );
        Question question = questionRepository.save(new Question("title", "content", QuestionCategory.BACKEND));
        subscribeQuestionRepository.save(SubscribeQuestion.success(subscribe, question));
        DailyMailMessage message = new DailyMailMessage(subscribe, question, "subject", "text");

        writer.write(new Chunk<>(List.of(message)));

        List<SubscribeQuestion> questions = subscribeQuestionRepository.findAll();
        List<ForwardLog> forwardLogs = forwardRepository.findAll();

        assertAll(
                () -> assertThat(questions).hasSize(1),
                () -> assertThat(questions.get(0).isSuccess()).isTrue(),
                () -> assertThat(forwardLogs).hasSize(1),
                () -> assertThat(forwardLogs.get(0).getTo()).isEqualTo("daily@test.com"),
                () -> assertThat(forwardLogs.get(0).getSubject()).isEqualTo("subject"),
                () -> assertThat(forwardLogs.get(0).getText()).isEqualTo("text"),
                () -> assertThat(forwardLogs.get(0).getStatus()).isEqualTo(ForwardStatus.PENDING)
        );
    }
}
