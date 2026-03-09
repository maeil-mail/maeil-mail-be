package maeilbatch.forward;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

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
    @DisplayName("단건 상태 업데이트를 수행한다.")
    void changeStateSingle() {
        ForwardLog log = forwardRepository.save(new ForwardLog("one@test.com", "subject1", "message1"));

        forwardDao.changeState(log.getId(), ForwardStatus.DONE);

        ForwardLog result = forwardRepository.findById(log.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(ForwardStatus.DONE);
    }

    private List<ForwardLog> createForwardLogs() {
        ForwardLog pendingLog = new ForwardLog("one@test.com", "subject1", "message1");
        ForwardLog failedLog = new ForwardLog("two@test.com", "subject2", "message2");
        failedLog.setStatus(ForwardStatus.FAILED);

        return forwardRepository.saveAll(List.of(pendingLog, failedLog));
    }
}
