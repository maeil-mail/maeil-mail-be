package maeilbatch.forward;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import maeilbatch.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ForwardSenderTest extends IntegrationTestSupport {

    @Autowired
    private ForwardRepository forwardRepository;

    @Autowired
    private ForwardSender forwardSender;

    @Test
    @DisplayName("전송 성공 시 상태를 DONE으로 변경한다.")
    void handleSuccess() {
        ForwardLog forwardLog = createForwardLog("success@test.com");

        forwardSender.handleSuccess(forwardLog);

        ForwardLog result = forwardRepository.findById(forwardLog.getId()).orElseThrow();
        assertAll(
                () -> assertThat(forwardLog.getStatus()).isEqualTo(ForwardStatus.DONE),
                () -> assertThat(result.getStatus()).isEqualTo(ForwardStatus.DONE)
        );
    }

    @Test
    @DisplayName("전송 실패 시 상태를 FAILED로 변경한다.")
    void handleFailure() {
        ForwardLog forwardLog = createForwardLog("fail@test.com");

        forwardSender.handleFailure(forwardLog);

        ForwardLog result = forwardRepository.findById(forwardLog.getId()).orElseThrow();
        assertAll(
                () -> assertThat(forwardLog.getStatus()).isEqualTo(ForwardStatus.FAILED),
                () -> assertThat(result.getStatus()).isEqualTo(ForwardStatus.FAILED)
        );
    }

    private ForwardLog createForwardLog(String target) {
        return forwardRepository.save(new ForwardLog(target, "subject", "message"));
    }
}
