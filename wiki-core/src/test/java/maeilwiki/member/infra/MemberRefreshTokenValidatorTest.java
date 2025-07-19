package maeilwiki.member.infra;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import maeilwiki.common.IntegrationTestSupport;
import maeilwiki.member.application.MemberIdentityException;
import maeilwiki.member.domain.Member;
import maeilwiki.member.domain.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MemberRefreshTokenValidatorTest extends IntegrationTestSupport {

    @Autowired
    private MemberRefreshTokenValidator memberRefreshTokenValidator;

    @Autowired
    private MemberTokenGenerator memberTokenGenerator;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("엑세스 토큰은 리프레시 토큰으로 사용될 수 없다.")
    void cantValidateAccess() {
        Member member = createMember();
        String accessToken = memberTokenGenerator.generateAccessToken(member);

        assertThatThrownBy(() -> memberRefreshTokenValidator.validateRefreshToken(accessToken))
                .isInstanceOf(MemberIdentityException.class);
    }

    private Member createMember() {
        Member member = new Member(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "GITHUB");
        member.setRefreshToken(memberTokenGenerator.generateRefreshToken());

        return memberRepository.save(member);
    }
}
