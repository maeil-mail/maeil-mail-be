package maeilbatch.forward;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ForwardProcessorTest {

    @Test
    @DisplayName("현재 진행 중이거나 완료된 전송 기록은 생략해야 한다.")
    void filtering() {
        ForwardProcessor forwardProcessor = new ForwardProcessor();
        ForwardLog pendingLog = createForwardLog("pending@kakao.com");
        ForwardLog failedLog = createForwardLog("failed@kakao.com", ForwardStatus.FAILED);
        ForwardLog processingLog = createForwardLog("processing@kakao.com", ForwardStatus.PROCESSING);
        ForwardLog doneLog = createForwardLog("done@kakao.com", ForwardStatus.DONE);

        ForwardLog pendingResult = forwardProcessor.process(pendingLog);
        ForwardLog failedResult = forwardProcessor.process(failedLog);
        ForwardLog processingResult = forwardProcessor.process(processingLog);
        ForwardLog doneResult = forwardProcessor.process(doneLog);

        assertAll(
                () -> assertThat(pendingResult).isEqualTo(pendingLog),
                () -> assertThat(failedResult).isEqualTo(failedLog),
                () -> assertThat(processingResult).isNull(),
                () -> assertThat(doneResult).isNull()
        );
    }

    private ForwardLog createForwardLog(String target) {
        return new ForwardLog(target, "subject", "message");
    }

    private ForwardLog createForwardLog(String target, ForwardStatus status) {
        ForwardLog forwardLog = createForwardLog(target);
        forwardLog.setStatus(status);

        return forwardLog;
    }
}
