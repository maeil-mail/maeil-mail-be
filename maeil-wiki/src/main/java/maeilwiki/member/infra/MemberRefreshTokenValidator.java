package maeilwiki.member.infra;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import maeilwiki.member.application.MemberIdentityException;
import org.springframework.stereotype.Component;

@Component
public class MemberRefreshTokenValidator {

    private final JwtParser jwtParser;

    public MemberRefreshTokenValidator(MemberTokenProperties properties) {
        this.jwtParser = Jwts.parser()
                .require("type", "refresh")
                .verifyWith(properties.secretKey())
                .build();
    }

    public void validateRefreshToken(String token) {
        try {
            jwtParser.parseSignedClaims(token);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new MemberIdentityException(exception);
        }
    }
}
