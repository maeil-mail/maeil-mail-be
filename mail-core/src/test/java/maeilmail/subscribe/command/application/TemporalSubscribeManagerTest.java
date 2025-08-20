package maeilmail.subscribe.command.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import maeilmail.subscribe.command.domain.TemporalSubscribe;
import maeilmail.subscribe.command.domain.TemporalSubscribeRepository;
import maeilmail.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TemporalSubscribeManagerTest extends IntegrationTestSupport {

    @Autowired
    private TemporalSubscribeManager temporalSubscribeManager;

    @Autowired
    private TemporalSubscribeRepository temporalSubscribeRepository;

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
    void canVerify() {
        temporalSubscribeManager.add("test3@naver.com", "3212");

        assertThatCode(() -> temporalSubscribeManager.verify("test3@naver.com", "3212"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("기존에 저장된 인증 코드가 N개인 경우, 하나의 인증 코드만 있어도 검증에 성공한다.")
    void canVerify2() {
        createTemporalSubscribe("test4@naver.com", "3212");
        createTemporalSubscribe("test4@naver.com", "2111");
        createTemporalSubscribe("test4@naver.com", "3222");
        createTemporalSubscribe("test4@naver.com", "3221");

        assertThatCode(() -> temporalSubscribeManager.verify("test4@naver.com", "3222"))
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

    @Test
    @DisplayName("기존에 저장된 인증 코드가 N개인 경우, 모든 인증 코드를 제거할 수 있다.")
    void deleteAllBefore() {
        createTemporalSubscribe("test4@naver.com", "3212");
        createTemporalSubscribe("test4@naver.com", "2111");
        createTemporalSubscribe("test4@naver.com", "3222");
        createTemporalSubscribe("test4@naver.com", "3221");

        temporalSubscribeManager.add("test4@naver.com", "1234");

        assertThatCode(() -> temporalSubscribeManager.verify("test4@naver.com", "1234"))
                .doesNotThrowAnyException();
    }


    private void createTemporalSubscribe(String email, String verifyCode) {
        TemporalSubscribe temporalSubscribe = new TemporalSubscribe(email, verifyCode);

        temporalSubscribeRepository.save(temporalSubscribe);
    }
}
