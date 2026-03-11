package maeilbatch.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import maeilbatch.forward.ForwardLog;
import maeilbatch.forward.ForwardRepository;
import maeilbatch.forward.ForwardStatus;
import maeilbatch.support.IntegrationTestSupport;
import maeilmail.mail.MailViewRenderer;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionRepository;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import maeilmail.subscribe.command.domain.SubscribeQuestionRepository;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
class MailGenerateStepTest extends IntegrationTestSupport {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SubscribeQuestionRepository subscribeQuestionRepository;

    @Autowired
    private ForwardRepository forwardRepository;

    @Autowired
    private MailViewRenderer mailViewRenderer;

    @BeforeEach
    void setUp() {
        reset(mailViewRenderer);
        configureRendererBehavior();
    }

    @AfterEach
    void tearDown() {
        forwardRepository.deleteAll();
        subscribeQuestionRepository.deleteAll();
        questionRepository.deleteAll();
        subscribeRepository.deleteAll();
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    @DisplayName("월요일에는 일간/주간 구독자의 전송 질문 이력과 메일 전송 내역을 생성한다.")
    void generateDailyAndWeeklyOnMonday() {
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 5, 5, 7, 0); // Monday
        setJpaAuditingTime(baseDateTime.minusHours(1));
        Subscribe daily = createSubscribe("daily@test.com", SubscribeFrequency.DAILY);
        Subscribe weekly = createSubscribe("weekly@test.com", SubscribeFrequency.WEEKLY);
        List<Question> questions = createQuestions(10);

        JobExecution result = jobLauncherTestUtils.launchStep("mailGenerateStep", toJobParameters(baseDateTime));

        List<ForwardLog> logs = forwardRepository.findAll();
        List<String> targets = logs.stream()
                .map(ForwardLog::getTarget)
                .toList();
        List<SubscribeQuestion> weeklyHistory = subscribeQuestionRepository.findBySubscribeAndQuestionIn(weekly, questions.subList(0, 5));
        boolean dailyHistoryExists = subscribeQuestionRepository.findBySubscribeAndQuestion(daily, questions.get(0)).isPresent();

        assertAll(
                () -> assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED),
                () -> assertThat(logs).hasSize(2),
                () -> assertThat(targets).containsExactlyInAnyOrder("daily@test.com", "weekly@test.com"),
                () -> assertThat(logs).allMatch(it -> it.getStatus() == ForwardStatus.PENDING),
                () -> assertThat(dailyHistoryExists).isTrue(),
                () -> assertThat(weeklyHistory).hasSize(SubscribeFrequency.WEEKLY.getSendCount())
        );
    }

    @Test
    @DisplayName("주간 비발송일에는 일간 구독자만 처리한다.")
    void generateOnlyDailyWhenNotMonday() {
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 5, 6, 7, 0); // Tuesday
        setJpaAuditingTime(baseDateTime.minusHours(1));
        Subscribe daily = createSubscribe("daily@test.com", SubscribeFrequency.DAILY);
        Subscribe weekly = createSubscribe("weekly@test.com", SubscribeFrequency.WEEKLY);
        List<Question> questions = createQuestions(10);

        JobExecution result = jobLauncherTestUtils.launchStep("mailGenerateStep", toJobParameters(baseDateTime));

        List<ForwardLog> logs = forwardRepository.findAll();
        boolean dailyHistoryExists = subscribeQuestionRepository.findBySubscribeAndQuestion(daily, questions.get(0)).isPresent();
        List<SubscribeQuestion> weeklyHistory = subscribeQuestionRepository.findBySubscribeAndQuestionIn(weekly, questions);

        assertAll(
                () -> assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED),
                () -> assertThat(logs).hasSize(1),
                () -> assertThat(logs.get(0).getTarget()).isEqualTo("daily@test.com"),
                () -> assertThat(dailyHistoryExists).isTrue(),
                () -> assertThat(weeklyHistory).isEmpty()
        );
    }

    @Test
    @DisplayName("해지 구독자와 기준 시각 이후 생성된 구독자는 생성 대상에서 제외된다.")
    void skipUnsubscribedAndFutureCreated() {
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 5, 5, 7, 0); // Monday
        setJpaAuditingTime(baseDateTime.minusHours(1));
        createSubscribe("daily@test.com", SubscribeFrequency.DAILY);
        Subscribe unsubscribed = createSubscribe("unsubscribed@test.com", SubscribeFrequency.DAILY);
        unsubscribed.unsubscribe();
        subscribeRepository.save(unsubscribed);

        setJpaAuditingTime(baseDateTime.plusHours(1));
        createSubscribe("future@test.com", SubscribeFrequency.DAILY);
        createQuestions(10);

        JobExecution result = jobLauncherTestUtils.launchStep("mailGenerateStep", toJobParameters(baseDateTime));

        List<ForwardLog> logs = forwardRepository.findAll();
        List<String> targets = logs.stream()
                .map(ForwardLog::getTarget)
                .toList();

        assertAll(
                () -> assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED),
                () -> assertThat(logs).hasSize(1),
                () -> assertThat(targets).containsExactly("daily@test.com"),
                () -> assertThat(targets).doesNotContain("unsubscribed@test.com", "future@test.com")
        );
    }

    @Test
    @DisplayName("기존 전송 이력이 있어도 중복 저장하지 않고 최신 이력으로 교체한다.")
    void replaceAlreadySentHistoryWithoutDuplicate() {
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 5, 5, 7, 0); // Monday
        setJpaAuditingTime(baseDateTime.minusHours(1));
        Subscribe daily = createSubscribe("daily@test.com", SubscribeFrequency.DAILY);
        Subscribe weekly = createSubscribe("weekly@test.com", SubscribeFrequency.WEEKLY);
        List<Question> questions = createQuestions(10);
        createSentHistory(daily, questions.subList(0, 1), baseDateTime.minusMinutes(30));
        createSentHistory(weekly, questions.subList(0, 5), baseDateTime.minusMinutes(30));
        setJpaAuditingTime(baseDateTime.plusMinutes(10));

        JobExecution result = jobLauncherTestUtils.launchStep("mailGenerateStep", toJobParameters(baseDateTime));

        List<SubscribeQuestion> dailyHistory = subscribeQuestionRepository.findBySubscribeAndQuestionIn(
                daily,
                questions.subList(0, 1)
        );
        List<SubscribeQuestion> weeklyHistory = subscribeQuestionRepository.findBySubscribeAndQuestionIn(
                weekly,
                questions.subList(0, 5)
        );
        List<ForwardLog> logs = forwardRepository.findAll();

        assertAll(
                () -> assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED),
                () -> assertThat(dailyHistory).hasSize(1),
                () -> assertThat(weeklyHistory).hasSize(5),
                () -> assertThat(dailyHistory)
                        .extracting(SubscribeQuestion::getCreatedAt)
                        .allMatch(it -> it.equals(baseDateTime.plusMinutes(10))),
                () -> assertThat(weeklyHistory)
                        .extracting(SubscribeQuestion::getCreatedAt)
                        .allMatch(it -> it.equals(baseDateTime.plusMinutes(10))),
                () -> assertThat(logs).hasSize(2)
        );
    }

    @Test
    @DisplayName("processor에서 예외가 발생한 구독자는 건너뛰고 나머지는 생성한다.")
    void skipWhenProcessorThrowsException() {
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 5, 5, 7, 0); // Monday
        setJpaAuditingTime(baseDateTime.minusHours(1));
        // FRONTEND 질문만 준비해서 BACKEND 구독자는 질문 선택 실패를 유도한다.
        createSubscribe("valid-daily@test.com", SubscribeFrequency.DAILY);
        createSubscribe("invalid-weekly@test.com", SubscribeFrequency.WEEKLY, QuestionCategory.BACKEND);
        createQuestions(10);

        JobExecution result = jobLauncherTestUtils.launchStep("mailGenerateStep", toJobParameters(baseDateTime));

        List<ForwardLog> logs = forwardRepository.findAll();
        List<String> targets = logs.stream()
                .map(ForwardLog::getTarget)
                .toList();

        assertAll(
                () -> assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED),
                () -> assertThat(logs).hasSize(1),
                () -> assertThat(targets).containsExactly("valid-daily@test.com"),
                () -> assertThat(targets).doesNotContain("invalid-weekly@test.com")
        );
    }

    @Test
    @DisplayName("created_at이 기준 시각과 같으면 생성 대상에 포함된다.")
    void includeWhenCreatedAtEqualsDatetime() {
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 5, 5, 7, 0); // Monday
        setJpaAuditingTime(baseDateTime);
        createSubscribe("equal@test.com", SubscribeFrequency.DAILY);
        setJpaAuditingTime(baseDateTime.plusSeconds(1));
        createSubscribe("future@test.com", SubscribeFrequency.DAILY);
        createQuestions(10);

        JobExecution result = jobLauncherTestUtils.launchStep("mailGenerateStep", toJobParameters(baseDateTime));

        List<ForwardLog> logs = forwardRepository.findAll();
        List<String> targets = logs.stream()
                .map(ForwardLog::getTarget)
                .toList();

        assertAll(
                () -> assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED),
                () -> assertThat(logs).hasSize(1),
                () -> assertThat(targets).containsExactly("equal@test.com"),
                () -> assertThat(targets).doesNotContain("future@test.com")
        );
    }

    @Test
    @DisplayName("실패 후 동일 파라미터로 재실행하면 실패 지점부터 이어서 처리하고 중복 저장하지 않는다.")
    void restartFromFailedPointWithoutDuplicate() {
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 5, 5, 7, 0); // Monday
        setJpaAuditingTime(baseDateTime.minusHours(1));
        createSubscribes("daily", 120, SubscribeFrequency.DAILY);
        createQuestions(10);
        String failTargetEmail = "daily-105@test.com";
        JobParameters jobParameters = toJobParameters(baseDateTime, 1L);

        // 렌더러에서 Error를 발생하도록 해, Step 실패를 유도한다.
        configureRendererFatalError(failTargetEmail);
        JobExecution failedExecution = jobLauncherTestUtils.launchStep("mailGenerateStep", jobParameters);
        long firstRunLogCount = forwardRepository.count();

        configureRendererBehavior();
        JobExecution rerunExecution = jobLauncherTestUtils.launchStep("mailGenerateStep", jobParameters);

        List<ForwardLog> allLogs = forwardRepository.findAll();
        List<String> allTargets = allLogs.stream()
                .map(ForwardLog::getTarget)
                .toList();
        long uniqueTargetCount = allTargets.stream()
                .distinct()
                .count();

        assertAll(
                () -> assertThat(failedExecution.getStatus()).isEqualTo(BatchStatus.FAILED),
                () -> assertThat(firstRunLogCount).isEqualTo(100),
                () -> assertThat(rerunExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED),
                () -> assertThat(allLogs).hasSize(120),
                () -> assertThat(uniqueTargetCount).isEqualTo(120L)
        );
    }

    private Subscribe createSubscribe(String email, SubscribeFrequency frequency) {
        return createSubscribe(email, frequency, QuestionCategory.FRONTEND);
    }

    private Subscribe createSubscribe(String email, SubscribeFrequency frequency, QuestionCategory category) {
        return subscribeRepository.save(new Subscribe(email, category, frequency));
    }

    private List<Question> createQuestions(int size) {
        List<Question> questions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            questions.add(new Question("title-" + i, "content-" + i, QuestionCategory.FRONTEND));
        }

        return questionRepository.saveAll(questions);
    }

    private void createSentHistory(Subscribe subscribe, List<Question> questions, LocalDateTime createdAt) {
        setJpaAuditingTime(createdAt);
        subscribeQuestionRepository.saveAll(
                questions.stream()
                        .map(question -> SubscribeQuestion.success(subscribe, question))
                        .toList()
        );
    }

    private void createSubscribes(String prefix, int size, SubscribeFrequency frequency) {
        for (int i = 0; i < size; i++) {
            createSubscribe(prefix + "-" + i + "@test.com", frequency);
        }
    }

    private void configureRendererBehavior() {
        when(mailViewRenderer.render(anyMap(), anyString()))
                .thenReturn("mock-rendered-text");
    }

    private void configureRendererFatalError(String failTargetEmail) {
        doAnswer(invocation -> {
            Object email = invocation.getArgument(0, java.util.Map.class).get("email");
            if (failTargetEmail.equals(email)) {
                throw new AssertionError("mock renderer fatal error");
            }

            return "mock-rendered-text";
        }).when(mailViewRenderer).render(anyMap(), anyString());
    }

    private JobParameters toJobParameters(LocalDateTime baseDateTime) {
        return toJobParameters(baseDateTime, System.nanoTime());
    }

    private JobParameters toJobParameters(LocalDateTime baseDateTime, long runId) {
        return new JobParametersBuilder()
                .addString("datetime", baseDateTime.toString())
                .addLong("run.id", runId)
                .toJobParameters();
    }
}
