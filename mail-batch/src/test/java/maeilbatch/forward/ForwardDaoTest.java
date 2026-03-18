package maeilbatch.forward;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import maeilbatch.support.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ForwardDaoTest extends IntegrationTestSupport {

    @Autowired
    private ForwardDao forwardDao;

    @Autowired
    private ForwardRepository forwardRepository;

    @AfterEach
    void tearDownForwardLog() {
        forwardRepository.deleteAll();
    }

    @Test
    @DisplayName("전달한 로그 목록을 상태와 id 기준으로 일괄 업데이트한다.")
    void changeStateBatch() {
        List<ForwardLog> logs = createForwardLogs();

        forwardDao.changeState(logs, ForwardStatus.PROCESSING);

        List<Long> ids = logs.stream()
                .map(ForwardLog::getId)
                .toList();
        List<ForwardStatus> statuses = forwardRepository.findAllById(ids).stream()
                .map(ForwardLog::getStatus)
                .toList();

        assertAll(
                () -> assertThat(logs).allMatch(it -> it.getStatus() == ForwardStatus.PROCESSING),
                () -> assertThat(statuses).containsExactlyInAnyOrder(ForwardStatus.PROCESSING, ForwardStatus.PROCESSING)
        );
    }

    @Test
    @DisplayName("빈 목록으로 상태 변경을 요청하면 아무 것도 변경하지 않는다.")
    void changeStateEmptyLogsNoOp() {
        List<ForwardLog> logs = createForwardLogs();
        List<Long> ids = logs.stream()
                .map(ForwardLog::getId)
                .toList();

        forwardDao.changeState(List.of(), ForwardStatus.DONE);

        List<ForwardStatus> statuses = forwardRepository.findAllById(ids).stream()
                .map(ForwardLog::getStatus)
                .toList();

        assertThat(statuses)
                .containsExactlyInAnyOrder(ForwardStatus.PENDING, ForwardStatus.FAILED);
    }

    @Test
    @DisplayName("전달한 로그 목록을 일괄 저장한다.")
    void batchInsert() {
        ForwardLog pendingLog = new ForwardLog("one@test.com", "subject1", "message1");
        ForwardLog failedLog = new ForwardLog("two@test.com", "subject2", "message2");
        failedLog.setStatus(ForwardStatus.FAILED);

        forwardDao.batchInsert(List.of(pendingLog, failedLog));

        List<ForwardLog> result = forwardRepository.findAll();
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result)
                        .extracting(ForwardLog::getTarget)
                        .containsExactlyInAnyOrder("one@test.com", "two@test.com"),
                () -> assertThat(result)
                        .extracting(ForwardLog::getStatus)
                        .containsExactlyInAnyOrder(ForwardStatus.PENDING, ForwardStatus.FAILED)
        );
    }

    @Test
    @DisplayName("빈 목록으로 일괄 저장을 요청하면 아무 것도 저장하지 않는다.")
    void batchInsertEmptyLogsNoOp() {
        forwardDao.batchInsert(List.of());

        assertThat(forwardRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("지정한 날짜 범위의 최소/최대 id를 조회한다.")
    void queryIdRange() {
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 5, 5, 7, 0);
        saveForwardLogs(2, baseDateTime.minusMinutes(1), ForwardStatus.PENDING);
        List<ForwardLog> inRangeLogs = saveForwardLogs(3, baseDateTime.plusMinutes(1), ForwardStatus.PENDING);
        saveForwardLogs(2, baseDateTime.plusDays(1), ForwardStatus.PENDING);

        ForwardIdRange idRange = forwardDao.queryIdRange(baseDateTime, baseDateTime.plusDays(1));

        long expectedMinId = inRangeLogs.stream()
                .mapToLong(ForwardLog::getId)
                .min()
                .orElseThrow();
        long expectedMaxId = inRangeLogs.stream()
                .mapToLong(ForwardLog::getId)
                .max()
                .orElseThrow();

        assertAll(
                () -> assertThat(idRange.minId()).isEqualTo(expectedMinId),
                () -> assertThat(idRange.maxId()).isEqualTo(expectedMaxId)
        );
    }

    @Test
    @DisplayName("지정한 날짜 범위에 로그가 없으면 id 범위는 0, 0을 반환한다.")
    void queryIdRangeWhenNoData() {
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 5, 5, 7, 0);
        saveForwardLogs(2, baseDateTime.minusDays(1), ForwardStatus.PENDING);
        saveForwardLogs(2, baseDateTime.plusDays(1), ForwardStatus.PENDING);

        ForwardIdRange idRange = forwardDao.queryIdRange(baseDateTime, baseDateTime.plusHours(1));

        assertAll(
                () -> assertThat(idRange.minId()).isZero(),
                () -> assertThat(idRange.maxId()).isZero(),
                () -> assertThat(idRange.isEmpty()).isTrue()
        );
    }

    private List<ForwardLog> createForwardLogs() {
        ForwardLog pendingLog = new ForwardLog("one@test.com", "subject1", "message1");
        ForwardLog failedLog = new ForwardLog("two@test.com", "subject2", "message2");
        failedLog.setStatus(ForwardStatus.FAILED);

        return forwardRepository.saveAll(List.of(pendingLog, failedLog));
    }

    private List<ForwardLog> saveForwardLogs(int size, LocalDateTime createdAt, ForwardStatus status) {
        setAuditingTime(createdAt);
        List<ForwardLog> logs = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            ForwardLog log = new ForwardLog("user-%s@test.com".formatted(i), "subject-%s".formatted(i), "message-%s".formatted(i));
            log.setStatus(status);
            logs.add(log);
        }

        return forwardRepository.saveAll(logs);
    }
}
