package maeilmail.subscribe.core;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import maeilmail.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TemporalSubscribeManagerTest extends IntegrationTestSupport {

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

    @Test
    @DisplayName("이미 인증 코드를 받은 적이 있다면, 기존 인증 코드를 제거한다.")
    void deleteBefore() {
        temporalSubscribeManager.add("test4@naver.com", "3212");
        temporalSubscribeManager.add("test4@naver.com", "2111");
        temporalSubscribeManager.add("test4@naver.com", "3222");
        temporalSubscribeManager.add("test4@naver.com", "1234");

        assertThatCode(() -> temporalSubscribeManager.verify("test4@naver.com", "1234"))
                .doesNotThrowAnyException();
    }
}
