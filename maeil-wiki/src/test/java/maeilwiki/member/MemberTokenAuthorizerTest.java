package maeilwiki.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import maeilwiki.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;

class MemberTokenAuthorizerTest extends IntegrationTestSupport {

    @Autowired
    private MemberTokenAuthorizer authorizer;

    @Autowired
    private MemberTokenGenerator memberTokenGenerator;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("주어진 인가 헤더를 이용해 신원을 식별한다.")
    void authorize() {
        Member member = createMember();
        String accessToken = memberTokenGenerator.generateAccessToken(member);

        Identity identity = authorizer.authorize(accessToken);

        assertThat(identity.id()).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("액세스 토큰이 아닌 토큰이 들어온 경우, 인가를 수행할 수 없다.")
    void refresh() {
        String refreshToken = memberTokenGenerator.generateRefreshToken();

        assertThatThrownBy(() -> authorizer.authorize(refreshToken))
                .isInstanceOf(IdentityException.class)
                .hasMessage("유효한 토큰이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("토큰이 만료된 경우, 인가를 수행할 수 없다.")
    void expired() {
        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzM3OTU4NjEwLCJleHAiOjE3Mzc5NTg2MTAsInBpY3R1cmUiOiJ3d3cubmF2ZXIuY29tIiwibmFtZSI6Im5hbWUiLCJ0eXBlIjoiYWNjZXNzIn0.jetrlK4CRvJzNVGC9yZNAIyJbaPVm5lasjW1FWNQNUY";

        assertThatThrownBy(() -> authorizer.authorize(expiredToken))
                .isInstanceOf(IdentityException.class)
                .hasMessage("유효한 토큰이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("변조된 토큰이 들어온 경우, 인가를 수행할 수 없다.")
    void modified() {
        Member member = createMember();
        String actualToken = memberTokenGenerator.generateAccessToken(member);
        String modifiedToken = modifyToken(actualToken);

        assertThatThrownBy(() -> authorizer.authorize(modifiedToken))
                .isInstanceOf(IdentityException.class)
                .hasMessage("유효한 토큰이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("토큰 알고리즘이 none인 경우, 인가를 수행할 수 없다.")
    void noneAlg() {
        Member member = createMember();
        String actualToken = memberTokenGenerator.generateAccessToken(member);
        String modifiedToken = modifyTokenAlgToNone(actualToken);

        assertThatThrownBy(() -> authorizer.authorize(modifiedToken))
                .isInstanceOf(IdentityException.class)
                .hasMessage("유효한 토큰이 존재하지 않습니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("주어진 토큰이 널, 빈문자열인 경우, 인가를 수행할 수 없다.")
    void empty(String source) {
        assertThatThrownBy(() -> authorizer.authorize(source))
                .isInstanceOf(IdentityException.class)
                .hasMessage("유효한 토큰이 존재하지 않습니다.");
    }

    private Member createMember() {
        Member member = new Member("name", "GH-1234", Provider.GITHUB, "www.naver.com");
        String refreshToken = memberTokenGenerator.generateRefreshToken();
        member.setRefreshToken(refreshToken);

        return memberRepository.save(member);
    }

    private String modifyToken(String actualToken) {
        String[] tokens = actualToken.split("\\.");
        String header = tokens[0];
        String signature = tokens[2];
        String actualDecodedPayload = new String(Base64.getDecoder().decode(tokens[1]));
        String modifiedDecodedPayload = actualDecodedPayload.replaceFirst("\"sub\":\"[0-9]+\"", "\"sub\":\"999999\"");

        Base64.Encoder encoder = Base64.getEncoder().withoutPadding();
        String modifiedPayload = encoder.encodeToString(modifiedDecodedPayload.getBytes(StandardCharsets.UTF_8));

        return header + "." + modifiedPayload + "." + signature;
    }

    private String modifyTokenAlgToNone(String actualToken) {
        String[] tokens = actualToken.split("\\.");
        String payload = tokens[1];
        String signature = tokens[2];
        String actualDecodedHeader = new String(Base64.getDecoder().decode(tokens[0]));
        String modifiedDecodedHeader = actualDecodedHeader.replace("\"alg\":\"HS256\"", "\"alg\":\"none\"");

        Base64.Encoder encoder = Base64.getEncoder().withoutPadding();
        String modifiedHeader = encoder.encodeToString(modifiedDecodedHeader.getBytes(StandardCharsets.UTF_8));

        return modifiedHeader + "." + payload + "." + signature;
    }
}
