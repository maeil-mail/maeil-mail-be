package maeilwiki.member.infra;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component
public class MemberRefreshTokenValidator {

    private final JwtParser jwtParser;
    private final MemberTokenExceptionHandler exceptionHandler;

    public MemberRefreshTokenValidator(MemberTokenProperties properties, MemberTokenExceptionHandler exceptionHandler) {
        this.jwtParser = Jwts.parser()
                .require("type", "refresh")
                .verifyWith(properties.secretKey())
                .build();
        this.exceptionHandler = exceptionHandler;
    }

    public void validateRefreshToken(String token) {
        exceptionHandler.handle(jwtParser::parseSignedClaims, token);
    }
}
