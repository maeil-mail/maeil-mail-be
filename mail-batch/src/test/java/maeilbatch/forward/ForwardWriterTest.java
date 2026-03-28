package maeilbatch.forward;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import maeilbatch.support.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ForwardWriterTest extends IntegrationTestSupport {

    @Autowired
    private ForwardDao forwardDao;

    @Autowired
    private ForwardRepository forwardRepository;

    @AfterEach
    void tearDownForwardLog() {
        forwardRepository.deleteAll();
    }

    @Test
    @DisplayName("writer는 전송 결과에 따라 forward 상태를 DONE, FAILED로 직접 반영한다.")
    void write_updatesForwardStatusBySendResult() {
        ForwardSender forwardSender = createForwardSender();
        ForwardWriter forwardWriter = new ForwardWriter(forwardDao, forwardSender);
        List<ForwardLog> logs = createForwardLogs();
        Chunk<ForwardLog> chunk = new Chunk<>(logs);

        forwardWriter.write(chunk);

        List<ForwardLog> savedLogs = forwardRepository.findAllById(logs.stream()
                .map(ForwardLog::getId)
                .toList());

        assertAll(
                () -> assertThat(savedLogs)
                        .extracting(ForwardLog::getTarget, ForwardLog::getStatus)
                        .containsExactlyInAnyOrder(
                                tuple("one@test.com", ForwardStatus.DONE),
                                tuple("two@test.com", ForwardStatus.DONE),
                                tuple("three@test.com", ForwardStatus.FAILED)
                        ),
                () -> verify(forwardSender, times(3)).sendMailSync(any(ForwardLog.class))
        );
    }

    private ForwardSender createForwardSender() {
        ForwardSender forwardSender = mock(ForwardSender.class);
        Map<String, ForwardStatus> sendResult = Map.of(
                "one@test.com", ForwardStatus.DONE,
                "two@test.com", ForwardStatus.DONE,
                "three@test.com", ForwardStatus.FAILED
        );

        doAnswer(invocation -> {
            ForwardLog log = invocation.getArgument(0);
            log.setStatus(sendResult.get(log.getTarget()));
            return null;
        }).when(forwardSender).sendMailSync(any(ForwardLog.class));

        return forwardSender;
    }

    private List<ForwardLog> createForwardLogs() {
        return forwardRepository.saveAll(List.of(
                new ForwardLog("one@test.com", "subject1", "message1"),
                new ForwardLog("two@test.com", "subject2", "message2"),
                new ForwardLog("three@test.com", "subject3", "message3")
        ));
    }
}
