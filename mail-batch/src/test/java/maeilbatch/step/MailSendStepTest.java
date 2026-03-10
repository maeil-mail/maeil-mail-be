package maeilbatch.step;

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
import java.util.Set;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import maeilbatch.forward.ForwardLog;
import maeilbatch.forward.ForwardRepository;
import maeilbatch.forward.ForwardStatus;
import maeilbatch.support.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class MailSendStepTest extends IntegrationTestSupport {

    private static final int CHUNK_SIZE = 100;

    @Autowired
    private ForwardRepository forwardRepository;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private JavaMailSender javaMailSender;

    @BeforeEach
    void setUp() {
        reset(javaMailSender);
    }

    @AfterEach
    void tearDown() {
        forwardRepository.deleteAll();
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    @DisplayName("저장된 메일 전송 내역을 읽어서 메일을 전송한다.")
    void sendStoredForwardLogs() {
        configureJavaMailSenderBehavior(Set.of(), Set.of());
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 5, 1, 7, 0);
        List<ForwardLog> sendTargets = saveForwardLogs(3, baseDateTime.plusMinutes(1), ForwardStatus.PENDING, "send");
        saveForwardLogs(2, baseDateTime.minusMinutes(1), ForwardStatus.PENDING, "before");
        saveForwardLogs(2, baseDateTime.minusMinutes(1), ForwardStatus.PENDING, "after");

        JobExecution result = jobLauncherTestUtils.launchStep("mailSendStep", toJobParameters(baseDateTime));

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        List<String> expectedTargets = sendTargets.stream()
                .map(ForwardLog::getTarget)
                .toList();

        assertAll(
                () -> verify(javaMailSender, times(sendTargets.size())).send(captor.capture()),
                () -> assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED),
                () -> assertThat(captor.getAllValues()).hasSize(sendTargets.size()),
                () -> {
                    List<String> capturedTargets = captor.getAllValues().stream()
                            .map(this::extractToUnchecked)
                            .toList();
                    assertThat(capturedTargets).containsExactlyInAnyOrderElementsOf(expectedTargets);
                }
        );
    }

    @Test
    @DisplayName("재처리 가능한 전송 내역(PENDING, FAILED)만 전송한다.")
    void onlyRetryableLogsAreSent() {
        configureJavaMailSenderBehavior(Set.of(), Set.of());
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 5, 1, 7, 0);
        List<ForwardLog> pendingLogs = saveForwardLogs(1, baseDateTime.plusMinutes(1), ForwardStatus.PENDING, "pending");
        List<ForwardLog> failedLogs = saveForwardLogs(1, baseDateTime.plusMinutes(1), ForwardStatus.FAILED, "failed");
        saveForwardLogs(1, baseDateTime.plusMinutes(1), ForwardStatus.PROCESSING, "processing");
        saveForwardLogs(1, baseDateTime.plusMinutes(1), ForwardStatus.DONE, "done");

        JobExecution result = jobLauncherTestUtils.launchStep("mailSendStep", toJobParameters(baseDateTime));

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        List<String> expectedTargets = new ArrayList<>();
        expectedTargets.addAll(pendingLogs.stream().map(ForwardLog::getTarget).toList());
        expectedTargets.addAll(failedLogs.stream().map(ForwardLog::getTarget).toList());

        assertAll(
                () -> verify(javaMailSender, times(2)).send(captor.capture()),
                () -> assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED),
                () -> assertThat(captor.getAllValues()).hasSize(2),
                () -> {
                    List<String> capturedTargets = captor.getAllValues().stream()
                            .map(this::extractToUnchecked)
                            .toList();
                    assertThat(capturedTargets).containsExactlyInAnyOrderElementsOf(expectedTargets);
                }
        );
    }

    @Test
    @DisplayName("메일 전송 도중 스텝이 실패하면, 실패한 청크의 메일 전송 내역은 PROCESSING 상태로 변경된다.")
    void failedChunkBecomesProcessing() {
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 5, 1, 7, 0);
        List<ForwardLog> logs = saveForwardLogs(CHUNK_SIZE + 20, baseDateTime.plusMinutes(1), ForwardStatus.PENDING, "fail");
        List<Long> firstChunkIds = logs.subList(0, CHUNK_SIZE).stream()
                .map(ForwardLog::getId)
                .toList();
        List<Long> secondChunkIds = logs.subList(CHUNK_SIZE, CHUNK_SIZE + 20).stream()
                .map(ForwardLog::getId)
                .toList();
        String failTargetEmail = logs.get(0).getTarget();
        configureJavaMailSenderBehavior(Set.of(), Set.of(failTargetEmail));

        JobExecution result = jobLauncherTestUtils.launchStep("mailSendStep", toJobParameters(baseDateTime));

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        List<ForwardStatus> firstChunkStatuses = forwardRepository.findAllById(firstChunkIds).stream()
                .map(ForwardLog::getStatus)
                .toList();
        List<ForwardStatus> secondChunkStatuses = forwardRepository.findAllById(secondChunkIds).stream()
                .map(ForwardLog::getStatus)
                .toList();

        assertAll(
                () -> verify(javaMailSender, times(1)).send(captor.capture()),
                () -> assertThat(result.getStatus()).isEqualTo(BatchStatus.FAILED),
                () -> assertThat(firstChunkStatuses).allMatch(it -> it == ForwardStatus.PROCESSING),
                () -> assertThat(secondChunkStatuses).allMatch(it -> it == ForwardStatus.PENDING),
                () -> assertThat(captor.getAllValues()).hasSize(1)
        );
    }

    @Test
    @DisplayName("메일 전송 중 Exception이 발생하면 해당 건은 FAILED, 나머지는 DONE으로 처리되고 스텝은 완료된다.")
    void exceptionFailureHandledAsFailedAndStepCompletes() {
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 5, 1, 7, 0);
        List<ForwardLog> logs = saveForwardLogs(5, baseDateTime.plusMinutes(1), ForwardStatus.PENDING, "exception");
        String exceptionTargetEmail = logs.get(0).getTarget();
        List<Long> ids = logs.stream()
                .map(ForwardLog::getId)
                .toList();
        configureJavaMailSenderBehavior(Set.of(exceptionTargetEmail), Set.of());

        JobExecution result = jobLauncherTestUtils.launchStep("mailSendStep", toJobParameters(baseDateTime));

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        List<ForwardStatus> statuses = forwardRepository.findAllById(ids).stream()
                .map(ForwardLog::getStatus)
                .toList();
        long failedCount = statuses.stream().filter(it -> it == ForwardStatus.FAILED).count();
        long doneCount = statuses.stream().filter(it -> it == ForwardStatus.DONE).count();

        assertAll(
                () -> verify(javaMailSender, times(5)).send(captor.capture()),
                () -> assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED),
                () -> assertThat(failedCount).isEqualTo(1L),
                () -> assertThat(doneCount).isEqualTo(4L),
                () -> assertThat(captor.getAllValues()).hasSize(5)
        );
    }

    @Test
    @DisplayName("스텝이 실패한 뒤 재실행하면 실패한 청크를 건너뛰고 남은 건을 전송한다.")
    void skipFailedChunkOnRerun() {
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 5, 1, 7, 0);
        List<ForwardLog> logs = saveForwardLogs(CHUNK_SIZE + 20, baseDateTime.plusMinutes(1), ForwardStatus.PENDING, "rerun");
        List<ForwardLog> firstChunkLogs = logs.subList(0, CHUNK_SIZE);
        List<Long> skipChunkIds = logs.subList(0, CHUNK_SIZE).stream()
                .map(ForwardLog::getId)
                .toList();
        List<ForwardLog> secondChunkLogs = logs.subList(CHUNK_SIZE, CHUNK_SIZE + 20);
        List<Long> sendTargetChunkIds = secondChunkLogs.stream()
                .map(ForwardLog::getId)
                .toList();
        String fatalTargetEmail = logs.get(0).getTarget();
        String exceptionTargetEmail = logs.get(CHUNK_SIZE).getTarget();
        configureJavaMailSenderBehavior(Set.of(), Set.of(fatalTargetEmail));

        // first run
        JobExecution failedExecution = jobLauncherTestUtils.launchStep("mailSendStep", toJobParameters(baseDateTime));
        assertThat(failedExecution.getStatus()).isEqualTo(BatchStatus.FAILED);

        // re-run
        configureJavaMailSenderBehavior(Set.of(exceptionTargetEmail), Set.of());
        JobExecution rerunExecution = jobLauncherTestUtils.launchStep("mailSendStep", toJobParameters(baseDateTime.plusSeconds(1)));

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        List<ForwardStatus> firstChunkStatuses = forwardRepository.findAllById(skipChunkIds).stream()
                .map(ForwardLog::getStatus)
                .toList();
        List<ForwardStatus> secondChunkStatuses = forwardRepository.findAllById(sendTargetChunkIds).stream()
                .map(ForwardLog::getStatus)
                .toList();
        List<String> firstChunkTargets = firstChunkLogs.stream().map(ForwardLog::getTarget).toList();
        List<String> expectedRerunTargets = secondChunkLogs.stream().map(ForwardLog::getTarget).toList();
        long secondChunkFailedCount = secondChunkStatuses.stream()
                .filter(it -> it == ForwardStatus.FAILED)
                .count();
        long secondChunkDoneCount = secondChunkStatuses.stream()
                .filter(it -> it == ForwardStatus.DONE)
                .count();

        assertAll(
                () -> verify(javaMailSender, times(21)).send(captor.capture()),
                () -> assertThat(rerunExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED),
                () -> assertThat(firstChunkStatuses).allMatch(it -> it == ForwardStatus.PROCESSING),
                () -> assertThat(secondChunkFailedCount).isEqualTo(1L),
                () -> assertThat(secondChunkDoneCount).isEqualTo(19L),
                () -> assertThat(captor.getAllValues()).hasSize(21),
                () -> {
                    List<String> rerunCapturedTargets = captor.getAllValues().subList(1, captor.getAllValues().size()).stream()
                            .map(this::extractToUnchecked)
                            .toList();
                    assertThat(rerunCapturedTargets).containsExactlyInAnyOrderElementsOf(expectedRerunTargets);
                    assertThat(rerunCapturedTargets).noneMatch(firstChunkTargets::contains);
                }
        );
    }

    private void configureJavaMailSenderBehavior(Set<String> exceptionTargets, Set<String> errorTargets) {
        when(javaMailSender.createMimeMessage())
                .thenAnswer(invocation -> new MimeMessage(Session.getInstance(new Properties())));

        doAnswer(invocation -> {
            MimeMessage mimeMessage = invocation.getArgument(0);
            String to = extractTo(mimeMessage);
            if (exceptionTargets.contains(to)) {
                throw new IllegalStateException("mock send exception");
            }
            if (errorTargets.contains(to)) {
                throw new AssertionError("mock send fatal error");
            }
            return null;
        }).when(javaMailSender).send(any(MimeMessage.class));
    }

    private String extractTo(MimeMessage mimeMessage) throws Exception {
        Address[] recipients = mimeMessage.getRecipients(Message.RecipientType.TO);
        if (recipients == null || recipients.length == 0) {
            return "";
        }
        Address recipient = recipients[0];
        if (recipient instanceof InternetAddress internetAddress) {
            return internetAddress.getAddress();
        }
        return recipient.toString();
    }

    private String extractToUnchecked(MimeMessage mimeMessage) {
        try {
            return extractTo(mimeMessage);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private JobParameters toJobParameters(LocalDateTime dateTime) {
        return new JobParametersBuilder()
                .addString("datetime", dateTime.toString())
                .addLong("run.id", System.nanoTime())
                .toJobParameters();
    }

    private List<ForwardLog> saveForwardLogs(int size, LocalDateTime createdAt, ForwardStatus status, String prefix) {
        setJpaAuditingTime(createdAt);
        List<ForwardLog> logs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ForwardLog log = new ForwardLog(prefix + "-" + i + "@test.com", "subject-" + i, "message-" + i);
            log.setStatus(status);
            logs.add(log);
        }

        return forwardRepository.saveAll(logs);
    }
}
