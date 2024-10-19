package maeilmail.subscribe.core;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TemporalSubscribeManagerTest {

    @Autowired
    private TemporalSubscribeManager temporalSubscribeManager;

    @Test
    @DisplayName("이메일이 인증되지 않으면 검증에 실패한다.")
    void cantVerify() {
        assertThatThrownBy(() -> temporalSubscribeManager.verify("test1@naver.com", "3211"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("인증되지 않은 이메일입니다.");
    }

    @Test
    @DisplayName("이메일 인증 코드가 다르면 검증에 실패한다.")
    void cantVerify2() {
        temporalSubscribeManager.add("test2@naver.com", "3212");

        assertThatThrownBy(() -> temporalSubscribeManager.verify("test2@naver.com", "3211"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("인증되지 않은 이메일입니다.");
    }

    @Test
    @DisplayName("이메일 인증 코드가 같으면 검증에 성공한다.")
    void cantVerify3() {
        temporalSubscribeManager.add("test3@naver.com", "3212");

        assertThatCode(() -> temporalSubscribeManager.verify("test3@naver.com", "3212"))
                .doesNotThrowAnyException();
    }
}
