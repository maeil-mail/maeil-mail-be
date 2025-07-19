package maeilwiki.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import maeilwiki.common.IntegrationTestSupport;
import maeilwiki.member.domain.Member;
import maeilwiki.member.domain.MemberRepository;
import maeilwiki.member.infra.MemberTokenAuthorizer;
import maeilwiki.member.infra.MemberTokenGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MemberServiceTest extends IntegrationTestSupport {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberTokenGenerator memberTokenGenerator;

    @Autowired
    private MemberTokenAuthorizer authorizer;

    @Test
    @DisplayName("존재하지 않는 식별자로 사용자를 조회할 수 없다.")
    void findById() {
        long unknownId = -1;

        assertThatThrownBy(() -> memberService.findById(unknownId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("사용자의 리프레시 토큰으로 엑세스 토큰을 갱신할 수 있다.")
    void refresh() {
        Member member = createMember();
        MemberRefreshRequest request = new MemberRefreshRequest(member.getRefreshToken());

        MemberTokenResponse response = memberService.refresh(request);

        assertThatCode(() -> authorizer.authorize(response.accessToken()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("리프레시에 해당되는 사용자가 존재하지 않는다면 예외를 발생한다.")
    void notFoundMemberWithUnknownRefresh() {
        String unknownRefreshToken = "unknown-token";
        MemberRefreshRequest request = new MemberRefreshRequest(unknownRefreshToken);

        assertThatThrownBy(() -> memberService.refresh(request))
                .isInstanceOf(MemberIdentityException.class);
    }

    @Nested
    @DisplayName("회원 등록 요청을 수행할 때")
    class ApplyCase {

        @Test
        @DisplayName("신규 회원인 경우에는 회원 가입한다.")
        void signUp() {
            MemberRequest request = new MemberRequest("oauthAccessToken");

            memberService.apply(request);

            List<Member> members = memberRepository.findAll();
            assertThat(members).hasSize(1);
        }

        @Test
        @DisplayName("리프레시가 만료된 기회원의 경우, 리프레시를 갱신하고 로그인을 수행한다.")
        void signInWithExpiredToken() {
            MemberRequest request = new MemberRequest("oauthAccessToken");
            MemberTokenResponse originResponse = memberService.apply(request);
            String expiredMemberRefreshToken = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI1NzZiOTliNC03Y2Q2LTRmNjctODNlMC0xZWZkMTcyNmZjMzIiLCJpYXQiOjExMzkzMjY4MTMsImV4cCI6MTI0MTkxODgxMywidHlwZSI6InJlZnJlc2gifQ.aNqtjEcJHHGyp2CuKh0JG0lrw250wXOniF2Ti85xJZI";
            memberRepository.findByRefreshToken(originResponse.refreshToken())
                    .ifPresent(it -> it.setRefreshToken(expiredMemberRefreshToken));

            MemberTokenResponse newResponse = memberService.apply(request);

            List<Member> members = memberRepository.findAll();
            assertAll(
                    () -> assertThat(members).hasSize(1),
                    () -> assertThat(originResponse.accessToken()).isNotEqualTo(newResponse.accessToken()),
                    () -> assertThat(expiredMemberRefreshToken).isNotEqualTo(newResponse.refreshToken())
            );
        }

        @Test
        @DisplayName("리프레시가 만료되지 않은 기회원의 경우, 리프레시를 갱신하지 않고 로그인을 수행한다.")
        void signInWithNoneExpiredToken() {
            MemberRequest request = new MemberRequest("oauthAccessToken");
            MemberTokenResponse originResponse = memberService.apply(request);

            MemberTokenResponse newResponse = memberService.apply(request);

            List<Member> members = memberRepository.findAll();
            assertAll(
                    () -> assertThat(members).hasSize(1),
                    () -> assertThat(originResponse.accessToken()).isNotEqualTo(newResponse.accessToken()),
                    () -> assertThat(originResponse.refreshToken()).isEqualTo(newResponse.refreshToken())
            );
        }
    }

    private Member createMember() {
        Member member = new Member(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "GITHUB");
        member.setRefreshToken(memberTokenGenerator.generateRefreshToken());

        return memberRepository.save(member);
    }
}
