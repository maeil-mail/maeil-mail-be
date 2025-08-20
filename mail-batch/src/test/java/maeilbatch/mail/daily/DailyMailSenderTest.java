package maeilbatch.mail.daily;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.List;
import maeilbatch.support.IntegrationTestSupport;
import maeilmail.mail.MimeMessageCustomizer;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionRepository;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import maeilmail.subscribe.command.domain.SubscribeQuestionRepository;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;

class DailyMailSenderTest extends IntegrationTestSupport {

    @Autowired
    private SubscribeQuestionRepository subscribeQuestionRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    @DisplayName("메일 전송에 성공하면 내역을 생성한다.")
    void handleSuccess() {
        LocalDateTime createdAt = LocalDateTime.of(2025, 5, 1, 7, 0, 0);
        setJpaAuditingTime(createdAt);
        DailyMailSender dailyMailSender = createDailyMailSender();
        DailyMailMessage message = createMessage(createSubscribe(), createQuestion());

        dailyMailSender.handleSuccess(message);

        List<SubscribeQuestion> result = subscribeQuestionRepository.findAll();
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.get(0).getCreatedAt()).isEqualTo(createdAt)
        );
    }

    @Test
    @DisplayName("이미 전송된 질문지라면, 기존 내역을 제거하고 신규 내역을 생성한다.")
    void handleSuccessAlreadySend() {
        setJpaAuditingTime(LocalDateTime.of(2025, 5, 1, 7, 0, 0));
        DailyMailSender dailyMailSender = createDailyMailSender();
        Subscribe subscribe = createSubscribe();
        Question question = createQuestion();
        SubscribeQuestion alreadySendSubscribeQuestion = SubscribeQuestion.success(subscribe, question);
        subscribeQuestionRepository.save(alreadySendSubscribeQuestion);
        LocalDateTime newCreatedAt = LocalDateTime.of(2025, 5, 2, 7, 0, 0);
        setJpaAuditingTime(newCreatedAt);

        DailyMailMessage message = createMessage(subscribe, question);
        dailyMailSender.handleSuccess(message);

        List<SubscribeQuestion> result = subscribeQuestionRepository.findAll();
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.get(0).getCreatedAt()).isEqualTo(newCreatedAt)
        );
    }

    private DailyMailSender createDailyMailSender() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessageCustomizer customizer = mock(MimeMessageCustomizer.class);

        return new DailyMailSender(mailSender, customizer, subscribeQuestionRepository);
    }

    private Subscribe createSubscribe() {
        Subscribe subscribe = new Subscribe("test@test.com", QuestionCategory.BACKEND, SubscribeFrequency.DAILY);

        return subscribeRepository.save(subscribe);
    }

    private Question createQuestion() {
        Question question = new Question("test1", "content", QuestionCategory.BACKEND);

        return questionRepository.save(question);
    }

    private DailyMailMessage createMessage(Subscribe subscribe, Question question) {
        return new DailyMailMessage(subscribe, question, "subject", "text");
    }
}
