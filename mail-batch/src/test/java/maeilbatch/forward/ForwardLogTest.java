package maeilbatch.forward;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ForwardLogTest {

    @Test
    @DisplayName("재처리가 가능한 상태인지 확인할 수 있다.")
    void isRetryable() {
        ForwardLog pendingLog = createForwardLog("pending@kakao.com");
        ForwardLog failedLog = createForwardLog("failed@kakao.com", ForwardStatus.FAILED);
        ForwardLog processingLog = createForwardLog("processing@kakao.com", ForwardStatus.PROCESSING);
        ForwardLog doneLog = createForwardLog("done@kakao.com", ForwardStatus.DONE);

        assertAll(
                () -> assertThat(pendingLog.isRetryable()).isTrue(),
                () -> assertThat(failedLog.isRetryable()).isTrue(),
                () -> assertThat(processingLog.isRetryable()).isFalse(),
                () -> assertThat(doneLog.isRetryable()).isFalse()
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
