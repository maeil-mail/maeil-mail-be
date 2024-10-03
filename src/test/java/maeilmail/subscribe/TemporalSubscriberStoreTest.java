package maeilmail.subscribe;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TemporalSubscriberStoreTest {

    @Test
    @DisplayName("이메일이 인증되지 않으면 검증에 실패한다.")
    void cantVerify() {
        assertThatThrownBy(() -> TemporalSubscriberStore.verify("test1@naver.com", "3211"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("인증되지 않은 이메일입니다.");
    }

    @Test
    @DisplayName("이메일 인증 코드가 다르면 검증에 실패한다.")
    void cantVerify2() {
        TemporalSubscriberStore.add("test2@naver.com", "3212");

        assertThatThrownBy(() -> TemporalSubscriberStore.verify("test2@naver.com", "3211"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("인증되지 않은 이메일입니다.");
    }

    @Test
    @DisplayName("이메일 인증 코드가 같으면 검증에 성공한다.")
    void cantVerify3() {
        TemporalSubscriberStore.add("test3@naver.com", "3212");

        assertThatCode(() -> TemporalSubscriberStore.verify("test3@naver.com", "3212"))
                .doesNotThrowAnyException();
    }
}
