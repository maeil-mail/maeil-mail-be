package maeilbatch.forward;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ForwardLogTest {

    @Test
    @DisplayName("재처리가 가능한 상태인지 확인할 수 있다.")
    void isRetryable() {
        ForwardLog pendingLog = new ForwardLog("pending@kakao.com", "subject", "message");
        ForwardLog failedLog = new ForwardLog("failed@kakao.com", "subject", "message");
        failedLog.setStatus(ForwardStatus.FAILED);
        ForwardLog processingLog = new ForwardLog("processing@kakao.com", "subject", "message");
        processingLog.setStatus(ForwardStatus.PROCESSING);
        ForwardLog doneLog = new ForwardLog("done@kakao.com", "subject", "message");
        doneLog.setStatus(ForwardStatus.DONE);

        assertAll(
                () -> assertThat(pendingLog.isRetryable()).isTrue(),
                () -> assertThat(failedLog.isRetryable()).isTrue(),
                () -> assertThat(processingLog.isRetryable()).isFalse(),
                () -> assertThat(doneLog.isRetryable()).isFalse()
        );
    }
}
