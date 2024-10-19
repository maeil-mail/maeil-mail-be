package maeilmail.subscribe.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TemporalSubscribeTest {

    @Test
    @DisplayName("인증 코드가 유효하지 않으면 구독이 불가능하다.")
    void invalidCode() {
        TemporalSubscribe temporalSubscribe = new TemporalSubscribe("leehaneul@gmail.com", "code");

        String invalidCode = "deco";
        assertThatThrownBy(() -> temporalSubscribe.verify(invalidCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("인증되지 않은 이메일입니다.");
    }

    @Test
    @DisplayName("인증 코드가 유효하면 구독이 가능하다.")
    void verify() {
        TemporalSubscribe temporalSubscribe = new TemporalSubscribe("leehaneul@gmail.com", "code");

        temporalSubscribe.verify("code");

        boolean verified = temporalSubscribe.isVerified();
        assertThat(verified).isTrue();
    }
}
