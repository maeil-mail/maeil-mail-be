package maeilbatch.mail.weekly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

class WeeklyMailSenderTest extends IntegrationTestSupport {

    private static final String SUBSCRIBE_EMAIL = "test@test.com";
    private static final String QUESTION_TITLE_FORMAT = "test%s";
    private static final int WEEKLY_SEND_COUNT = SubscribeFrequency.WEEKLY.getSendCount();

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
        WeeklyMailSender weeklyMailSender = createWeeklyMailSender();
        WeeklyMailMessage message = createMessage(createSubscribe(), createQuestions(WEEKLY_SEND_COUNT));

        weeklyMailSender.handleSuccess(message);

        List<SubscribeQuestion> result = subscribeQuestionRepository.findAll();
        assertAll(
                () -> assertThat(result).hasSize(WEEKLY_SEND_COUNT),
                () -> assertThat(result.get(0).getCreatedAt()).isEqualTo(createdAt)
        );
    }

    @Test
    @DisplayName("이미 전송된 질문지라면, 기존 내역을 제거하고 신규 내역을 생성한다.")
    void handleSuccessAlreadySend() {
        setJpaAuditingTime(LocalDateTime.of(2025, 5, 1, 7, 0, 0));
        WeeklyMailSender weeklyMailSender = createWeeklyMailSender();
        Subscribe subscribe = createSubscribe();
        List<Question> questions = createQuestions(WEEKLY_SEND_COUNT);
        createSentHistory(subscribe, questions.get(0));
        LocalDateTime newCreatedAt = LocalDateTime.of(2025, 5, 2, 7, 0, 0);
        setJpaAuditingTime(newCreatedAt);

        WeeklyMailMessage message = createMessage(subscribe, questions);
        weeklyMailSender.handleSuccess(message);

        List<SubscribeQuestion> result = subscribeQuestionRepository.findAll();
        assertAll(
                () -> assertThat(result).hasSize(WEEKLY_SEND_COUNT),
                () -> assertThat(result)
                        .map(SubscribeQuestion::getCreatedAt)
                        .allMatch(it -> it.equals(newCreatedAt))
        );
    }

    private WeeklyMailSender createWeeklyMailSender() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessageCustomizer customizer = mock(MimeMessageCustomizer.class);

        return new WeeklyMailSender(mailSender, customizer, subscribeQuestionRepository);
    }

    private Subscribe createSubscribe() {
        Subscribe subscribe = new Subscribe(SUBSCRIBE_EMAIL, QuestionCategory.BACKEND, SubscribeFrequency.WEEKLY);

        return subscribeRepository.save(subscribe);
    }

    private List<Question> createQuestions(int size) {
        List<Question> questions = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            Question question = new Question(QUESTION_TITLE_FORMAT.formatted(i), "content", QuestionCategory.BACKEND);
            questions.add(question);
        }

        return questionRepository.saveAll(questions);
    }

    private void createSentHistory(Subscribe subscribe, Question question) {
        subscribeQuestionRepository.save(SubscribeQuestion.success(subscribe, question));
    }

    private WeeklyMailMessage createMessage(Subscribe subscribe, List<Question> questions) {
        return new WeeklyMailMessage(subscribe, questions, "subject", "text");
    }
}
