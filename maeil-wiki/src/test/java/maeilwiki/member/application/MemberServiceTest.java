package maeilwiki.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import maeilwiki.member.domain.Member;
import maeilwiki.member.domain.MemberRepository;
import maeilwiki.member.infra.MemberTokenGenerator;
import maeilwiki.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MemberServiceTest extends IntegrationTestSupport {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberTokenGenerator memberTokenGenerator;

    @Test
    @DisplayName("존재하지 않는 식별자로 사용자를 조회할 수 없다.")
    void findById() {
        long unknownId = -1;

        assertThatThrownBy(() -> memberService.findById(unknownId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("기회원인 경우, 리프레시를 갱신하고 새로운 토큰을 발급한다. (로그인)")
    void signIn() {
        MemberRequest request = new MemberRequest("oauthAccessToken");
        MemberTokenResponse originResponse = memberService.apply(request);

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
        MemberRequest request = new MemberRequest("oauthAccessToken");

        MemberTokenResponse response = memberService.apply(request);

        List<Member> members = memberRepository.findAll();
        assertThat(members).hasSize(1);
    }

    @Test
    @DisplayName("사용자의 리프레시 토큰으로 엑세스 토큰을 갱신할 수 있다.")
    void refresh() {
        Member member = createMember();
        String previousRefreshToken = member.getRefreshToken();
        MemberRefreshRequest request = new MemberRefreshRequest(member.getRefreshToken());

        MemberTokenResponse response = memberService.refresh(request);

        Member foundMember = memberRepository.findById(member.getId())
                .orElseThrow();
        assertThat(foundMember.getRefreshToken()).isNotEqualTo(previousRefreshToken);
    }

    private Member createMember() {
        Member member = new Member(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "GITHUB");
        member.setRefreshToken(memberTokenGenerator.generateRefreshToken());

        return memberRepository.save(member);
    }
}
