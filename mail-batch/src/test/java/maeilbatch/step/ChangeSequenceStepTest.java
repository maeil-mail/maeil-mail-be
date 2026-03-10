package maeilbatch.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ChangeSequenceStepTest extends IntegrationTestSupport {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private SubscribeQuestionRepository subscribeQuestionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @AfterEach
    void tearDown() {
        subscribeQuestionRepository.deleteAll();
        subscribeRepository.deleteAll();
        questionRepository.deleteAll();
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    @DisplayName("오늘 받은 메일의 수만큼 구독자의 시퀀스를 증가시킨다.")
    void changeSequenceStep() {
        LocalDateTime baseDateTime = LocalDateTime.now()
                .withHour(7)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        setJpaAuditingTime(baseDateTime.minusHours(1));
        Subscribe dailyActive = createSubscribe("daily-active@test.com", SubscribeFrequency.DAILY);
        Subscribe weeklyActive = createSubscribe("weekly-active@test.com", SubscribeFrequency.WEEKLY);
        Subscribe unsubscribed = createUnsubscribedSubscribe("daily-unsubscribed@test.com", SubscribeFrequency.DAILY);
        List<Question> questions = createQuestions(7);

        Long dailyBefore = dailyActive.getNextQuestionSequence();
        Long weeklyBefore = weeklyActive.getNextQuestionSequence();
        Long unsubscribedBefore = unsubscribed.getNextQuestionSequence();

        createTodaySentHistory(baseDateTime, dailyActive, questions.subList(0, 1));
        createTodaySentHistory(baseDateTime, weeklyActive, questions.subList(1, 6));

        JobExecution result = jobLauncherTestUtils.launchStep("changeSequenceTasklet", toJobParameters(baseDateTime));

        Subscribe dailyAfter = findSubscribe(dailyActive.getId());
        Subscribe weeklyAfter = findSubscribe(weeklyActive.getId());
        Subscribe unsubscribedAfter = findSubscribe(unsubscribed.getId());

        assertAll(
                () -> assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED),
                () -> assertThat(dailyAfter.getNextQuestionSequence()).isEqualTo(dailyBefore + 1),
                () -> assertThat(weeklyAfter.getNextQuestionSequence()).isEqualTo(weeklyBefore + SubscribeFrequency.WEEKLY.getSendCount()),
                () -> assertThat(unsubscribedAfter.getNextQuestionSequence()).isEqualTo(unsubscribedBefore)
        );
    }

    private Subscribe createSubscribe(String email, SubscribeFrequency frequency) {
        return subscribeRepository.save(new Subscribe(email, QuestionCategory.FRONTEND, frequency));
    }

    private Subscribe createUnsubscribedSubscribe(String email, SubscribeFrequency frequency) {
        Subscribe subscribe = createSubscribe(email, frequency);
        subscribe.unsubscribe();

        return subscribeRepository.save(subscribe);
    }

    private List<Question> createQuestions(int size) {
        List<Question> questions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            questions.add(new Question("q" + i, "c" + i, QuestionCategory.FRONTEND));
        }

        return questionRepository.saveAll(questions);
    }

    private void createTodaySentHistory(LocalDateTime baseDateTime, Subscribe subscribe, List<Question> questions) {
        setJpaAuditingTime(baseDateTime.plusMinutes(10));
        questions.forEach(question -> subscribeQuestionRepository.save(SubscribeQuestion.success(subscribe, question)));
    }

    private JobParameters toJobParameters(LocalDateTime baseDateTime) {
        return new JobParametersBuilder()
                .addString("datetime", baseDateTime.toString())
                .addLong("run.id", System.nanoTime())
                .toJobParameters();
    }

    private Subscribe findSubscribe(Long subscribeId) {
        return subscribeRepository.findById(subscribeId).orElseThrow();
    }
}
