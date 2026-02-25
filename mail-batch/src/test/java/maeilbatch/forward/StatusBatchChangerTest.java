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
class StatusBatchChangerTest extends IntegrationTestSupport {

    @Autowired
    private StatusBatchChanger statusBatchChanger;

    @Autowired
    private ForwardRepository forwardRepository;

    @AfterEach
    void tearDownForwardLog() {
        forwardRepository.deleteAll();
    }

    @Test
    @DisplayName("전달한 로그 목록을 상태와 id 기준으로 일괄 업데이트한다.")
    void changeStateBatch() {
        ForwardLog log1 = new ForwardLog("one@test.com", "subject1", "message1");
        ForwardLog log2 = new ForwardLog("two@test.com", "subject2", "message2");
        log2.setStatus(ForwardStatus.FAILED);
        List<ForwardLog> logs = forwardRepository.saveAll(List.of(log1, log2));

        statusBatchChanger.changeState(logs, ForwardStatus.PROCESSING);

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
}
