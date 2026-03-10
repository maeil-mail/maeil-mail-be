package maeilbatch.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import maeilbatch.forward.ForwardLog;
import maeilbatch.forward.ForwardRepository;
import maeilbatch.forward.ForwardStatus;
import maeilbatch.support.IntegrationTestSupport;
import maeilmail.mail.MailSender;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionRepository;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeQuestionRepository;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class MailSendJobTest extends IntegrationTestSupport {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job mailSendJob;

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
    private JavaMailSender javaMailSender;

    @Autowired
    private MailSender mailSender;

    @BeforeEach
    void setUp() {
        reset(javaMailSender, mailSender);
        jobLauncherTestUtils.setJob(mailSendJob);
        configureMailSenderSuccess();
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
    @DisplayName("메일 전송 내역 및 받은 질문지 내역 저장 이후 질문지를 발송한다.(사후 작업으로 시퀀스 증가와 관리자 알림을 수행)")
    void mailSendJobHappyCase() throws Exception {
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 5, 5, 7, 0); // Monday
        setJpaAuditingTime(baseDateTime.minusHours(1));
        Subscribe dailySubscribe = createSubscribe("daily@test.com", SubscribeFrequency.DAILY);
        Subscribe weeklySubscribe = createSubscribe("weekly@test.com", SubscribeFrequency.WEEKLY);
        createQuestions(10);
        Long dailyBefore = dailySubscribe.getNextQuestionSequence();
        Long weeklyBefore = weeklySubscribe.getNextQuestionSequence();
        setJpaAuditingTime(baseDateTime.plusMinutes(1));

        JobExecution result = jobLauncherTestUtils.launchJob(toJobParameters(baseDateTime, 1L));

        List<ForwardLog> logs = forwardRepository.findAll();
        long doneCount = logs.stream()
                .filter(it -> it.getStatus() == ForwardStatus.DONE)
                .count();
        long subscribeQuestionCount = subscribeQuestionRepository.count();
        Subscribe dailyAfter = subscribeRepository.findById(dailySubscribe.getId()).orElseThrow();
        Subscribe weeklyAfter = subscribeRepository.findById(weeklySubscribe.getId()).orElseThrow();

        assertAll(
                () -> verify(javaMailSender, times((int) doneCount)).send(any(MimeMessage.class)),
                () -> verify(mailSender, times(1)).sendMail(any()),
                () -> assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED),
                () -> assertThat(logs).hasSize(2),
                () -> assertThat(doneCount).isEqualTo(2L),
                () -> assertThat(subscribeQuestionCount).isEqualTo(6L),
                () -> assertThat(dailyAfter.getNextQuestionSequence()).isEqualTo(dailyBefore + 1),
                () -> assertThat(weeklyAfter.getNextQuestionSequence()).isEqualTo(weeklyBefore + SubscribeFrequency.WEEKLY.getSendCount())
        );
    }

    @Test
    @DisplayName("mailSendStep 실패 후 동일 파라미터 재실행 시 실패 step부터 재시작한다.")
    void restartFromFailedStep() throws Exception {
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 5, 5, 7, 0); // Monday
        setJpaAuditingTime(baseDateTime.minusHours(1));
        createSubscribe("daily@test.com", SubscribeFrequency.DAILY);
        createQuestions(10);
        JobParameters params = toJobParameters(baseDateTime, 2L);
        setJpaAuditingTime(baseDateTime.plusMinutes(1));

        configureJavaMailFatalError();
        JobExecution failedExecution = jobLauncherTestUtils.launchJob(params);

        configureMailSenderSuccess();
        JobExecution rerunExecution = jobLauncherTestUtils.launchJob(params);

        List<String> rerunStepNames = rerunExecution.getStepExecutions().stream()
                .map(StepExecution::getStepName)
                .toList();
        List<ForwardLog> logs = forwardRepository.findAll();

        assertAll(
                () -> assertThat(failedExecution.getStatus()).isEqualTo(BatchStatus.FAILED),
                () -> assertThat(rerunExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED),
                () -> assertThat(rerunStepNames).doesNotContain("mailGenerateStep"),
                () -> assertThat(rerunStepNames).contains("mailSendStep", "changeSequenceTasklet"),
                () -> assertThat(subscribeQuestionRepository.count()).isEqualTo(1L),
                () -> assertThat(logs).hasSize(1),
                () -> assertThat(logs.get(0).getStatus()).isEqualTo(ForwardStatus.PROCESSING)
        );
    }

    private void configureMailSenderSuccess() {
        when(javaMailSender.createMimeMessage())
                .thenAnswer(invocation -> new MimeMessage(Session.getInstance(new Properties())));
    }

    private void configureJavaMailFatalError() {
        when(javaMailSender.createMimeMessage())
                .thenAnswer(invocation -> new MimeMessage(Session.getInstance(new Properties())));
        doAnswer(invocation -> {
            throw new AssertionError("mock fatal send error");
        }).when(javaMailSender).send(any(MimeMessage.class));
    }

    private Subscribe createSubscribe(String email, SubscribeFrequency frequency) {
        return subscribeRepository.save(new Subscribe(email, QuestionCategory.FRONTEND, frequency));
    }

    private List<Question> createQuestions(int size) {
        List<Question> questions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            questions.add(new Question("title-" + i, "content-" + i, QuestionCategory.FRONTEND));
        }

        return questionRepository.saveAll(questions);
    }

    private JobParameters toJobParameters(LocalDateTime baseDateTime, long runId) {
        return new JobParametersBuilder()
                .addString("datetime", baseDateTime.toString())
                .addLong("run.id", runId)
                .toJobParameters();
    }
}
