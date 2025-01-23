package maeilwiki.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Base64;
import maeilwiki.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MemberTokenGeneratorTest extends IntegrationTestSupport {

    @Autowired
    private MemberTokenGenerator memberTokenGenerator;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("HS254 알고리즘을 사용한다.")
    void checkAlg() {
        Member member = new Member("name", "test1234", "GITHUB");
        String refreshToken = memberTokenGenerator.generateRefreshToken();
        member.setRefreshToken(refreshToken);
        memberRepository.save(member);
        String accessToken = memberTokenGenerator.generateAccessToken(member);

        Base64.Decoder decoder = Base64.getDecoder();
        String decodedAccessTokenHeader = new String(decoder.decode(accessToken.split("\\.")[0]));
        String decodedRefreshTokenHeader = new String(decoder.decode(refreshToken.split("\\.")[0]));

        assertAll(
                () -> assertThat(decodedAccessTokenHeader).contains("HS256"),
                () -> assertThat(decodedRefreshTokenHeader).contains("HS256")
        );
    }
}
