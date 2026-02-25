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
        ForwardLog pendingLog = new ForwardLog("pending@kakao.com", "subject", "message");
        ForwardLog failedLog = new ForwardLog("failed@kakao.com", "subject", "message");
        failedLog.setStatus(ForwardStatus.FAILED);
        ForwardLog processingLog = new ForwardLog("processing@kakao.com", "subject", "message");
        processingLog.setStatus(ForwardStatus.PROCESSING);
        ForwardLog doneLog = new ForwardLog("done@kakao.com", "subject", "message");
        doneLog.setStatus(ForwardStatus.DONE);

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
}
