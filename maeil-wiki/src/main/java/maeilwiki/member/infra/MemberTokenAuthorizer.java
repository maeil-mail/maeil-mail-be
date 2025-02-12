package maeilwiki.member.infra;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import maeilwiki.member.application.MemberIdentity;
import org.springframework.stereotype.Component;

@Component
public class MemberTokenAuthorizer {

    private final JwtParser jwtParser;
    private final MemberTokenExceptionHandler exceptionHandler;

    public MemberTokenAuthorizer(MemberTokenProperties properties, MemberTokenExceptionHandler exceptionHandler) {
        this.jwtParser = Jwts.parser()
                .require("type", "access")
                .verifyWith(properties.secretKey())
                .build();
        this.exceptionHandler = exceptionHandler;
    }

    public MemberIdentity authorize(String token) {
        return exceptionHandler.handle(this::generateIdentity, token);
    }

    private MemberIdentity generateIdentity(String token) throws JwtException, IllegalArgumentException {
        Jws<Claims> claimsJws = jwtParser.parseSignedClaims(token);
        Claims payload = claimsJws.getPayload();
        String subject = payload.getSubject();
        String name = payload.get("name", String.class);
        String picture = payload.get("picture", String.class);

        return new MemberIdentity(Long.parseLong(subject), name, picture);
    }
}
