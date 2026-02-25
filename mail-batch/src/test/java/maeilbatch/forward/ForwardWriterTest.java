package maeilbatch.forward;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.persistence.EntityManager;
import java.util.List;
import maeilbatch.support.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.batch.item.Chunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ForwardWriterTest extends IntegrationTestSupport {

    @Autowired
    private StatusBatchChanger statusBatchChanger;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ForwardRepository forwardRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @AfterEach
    void tearDownForwardLog() {
        forwardRepository.deleteAll();
    }

    @Test
    @DisplayName("writer는 상태를 PROCESSING으로 변경하고 각 로그를 전송한다.")
    void write() {
        ForwardSender forwardSender = Mockito.mock(ForwardSender.class);
        ForwardWriter forwardWriter = new ForwardWriter(statusBatchChanger, forwardSender, entityManager);
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        List<ForwardLog> logs = tx.execute(status -> forwardRepository.saveAll(List.of(
                new ForwardLog("one@test.com", "subject1", "message1"),
                new ForwardLog("two@test.com", "subject2", "message2")
        )));
        Chunk<ForwardLog> chunk = new Chunk<>(logs);

        tx.executeWithoutResult(status -> forwardWriter.write(chunk));

        List<Long> ids = logs.stream()
                .map(ForwardLog::getId)
                .toList();
        List<ForwardStatus> statuses = forwardRepository.findAllById(ids).stream()
                .map(ForwardLog::getStatus)
                .toList();

        assertAll(
                () -> assertThat(statuses).containsExactlyInAnyOrder(ForwardStatus.PROCESSING, ForwardStatus.PROCESSING),
                () -> verify(forwardSender, times(2)).sendMailSync(any(ForwardLog.class))
        );
    }
}
