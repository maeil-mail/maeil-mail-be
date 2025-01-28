package maeilwiki.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.NoSuchElementException;
import maeilwiki.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MemberServiceTest extends IntegrationTestSupport {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("존재하지 않는 식별자로 사용자를 조회할 수 없다.")
    void findById() {
        long unknownId = -1;

        assertThatThrownBy(() -> memberService.findById(unknownId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("클라이언트 시크릿이 다르다면 회원 등록 처리를 할 수 없다.")
    void validateClientSecret() {
        MemberRequest memberRequest = new MemberRequest("accessToken", "unknownSecret");

        assertThatThrownBy(() -> memberService.apply(memberRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("올바른 클라이언트 암호를 입력해주세요.");
    }

    @Test
    @DisplayName("기회원인 경우, 리프레시를 갱신하고 새로운 토큰을 발급한다. (로그인)")
    void signIn() throws InterruptedException {
        MemberRequest request = new MemberRequest("accessToken", "clientSecret");
        MemberTokenResponse originResponse = memberService.apply(request);

        // 로그인 케이스에서 token 재발급 간격이 테스트 상에서 매우 짧아서 같은 토큰이 반환되기 때문에 Thread.sleep()을 추가했습니다.
        Thread.sleep(1000);
        MemberTokenResponse newResponse = memberService.apply(request);

        List<Member> members = memberRepository.findAll();
        assertAll(
                () -> assertThat(members).hasSize(1),
                () -> assertThat(originResponse).isNotEqualTo(newResponse)
        );
    }

    @Test
    @DisplayName("기회원이 아닌 경우, 회원 가입 처리를 한다. (회원가입)")
    void signUp() {
        MemberRequest request = new MemberRequest("accessToken", "clientSecret");

        MemberTokenResponse response = memberService.apply(request);

        List<Member> members = memberRepository.findAll();
        assertThat(members).hasSize(1);
    }
}
