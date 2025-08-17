package maeilmail.bulksend.sender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import maeilmail.mail.MimeMessageCustomizer;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionRepository;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import maeilmail.subscribe.command.domain.SubscribeQuestionRepository;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import maeilmail.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;

class WeeklyQuestionSenderTest extends IntegrationTestSupport {

    @Autowired
    private SubscribeQuestionRepository subscribeQuestionRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    @DisplayName("메일 전송에 성공하면 내역을 생성한다.")
    void handleSuccess() {
        int expectedQuestionsSize = 5;
        LocalDateTime createdAt = LocalDateTime.of(2025, 5, 1, 7, 0, 0);
        setJpaAuditingTime(createdAt);
        WeeklyQuestionSender weeklyQuestionSender = createWeeklyQuestionSender();
        WeeklySubscribeQuestionMessage message = createMessage(createSubscribe(), createQuestions(expectedQuestionsSize));

        weeklyQuestionSender.handleSuccess(message);

        List<SubscribeQuestion> result = subscribeQuestionRepository.findAll();
        assertAll(
                () -> assertThat(result).hasSize(expectedQuestionsSize),
                () -> assertThat(result.get(0).getCreatedAt()).isEqualTo(createdAt)
        );
    }

    @Test
    @DisplayName("이미 전송된 질문지라면, 기존 내역을 제거하고 신규 내역을 생성한다.")
    void handleSuccessAlreadySend() {
        int expectedQuestionsSize = 5;
        setJpaAuditingTime(LocalDateTime.of(2025, 5, 1, 7, 0, 0));
        WeeklyQuestionSender weeklyQuestionSender = createWeeklyQuestionSender();
        Subscribe subscribe = createSubscribe();
        List<Question> questions = createQuestions(expectedQuestionsSize);
        SubscribeQuestion alreadySendSubscribeQuestion = SubscribeQuestion.success(subscribe, questions.get(0));
        subscribeQuestionRepository.save(alreadySendSubscribeQuestion);
        LocalDateTime newCreatedAt = LocalDateTime.of(2025, 5, 2, 7, 0, 0);
        setJpaAuditingTime(newCreatedAt);

        WeeklySubscribeQuestionMessage message = createMessage(subscribe, questions);
        weeklyQuestionSender.handleSuccess(message);

        List<SubscribeQuestion> result = subscribeQuestionRepository.findAll();
        assertAll(
                () -> assertThat(result).hasSize(expectedQuestionsSize),
                () -> assertThat(result)
                        .map(SubscribeQuestion::getCreatedAt)
                        .allMatch(it -> it.equals(newCreatedAt))
        );
    }

    private WeeklyQuestionSender createWeeklyQuestionSender() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessageCustomizer customizer = mock(MimeMessageCustomizer.class);

        return new WeeklyQuestionSender(mailSender, customizer, subscribeQuestionRepository);
    }

    private Subscribe createSubscribe() {
        Subscribe subscribe = new Subscribe("test@test.com", QuestionCategory.BACKEND, SubscribeFrequency.DAILY);

        return subscribeRepository.save(subscribe);
    }

    private List<Question> createQuestions(int size) {
        List<Question> questions = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            Question question = new Question("test" + i, "content", QuestionCategory.BACKEND);
            questions.add(question);
        }

        return questionRepository.saveAll(questions);
    }

    private WeeklySubscribeQuestionMessage createMessage(Subscribe subscribe, List<Question> questions) {
        return new WeeklySubscribeQuestionMessage(subscribe, questions, "subject", "text");
    }
}
