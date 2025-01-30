package maeilwiki.member.infra;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import maeilwiki.member.application.MemberIdentity;
import maeilwiki.member.application.MemberIdentityException;
import org.springframework.stereotype.Component;

@Component
public class MemberTokenAuthorizer {

    private final JwtParser jwtParser;

    public MemberTokenAuthorizer(MemberTokenProperties properties) {
        this.jwtParser = Jwts.parser()
                .require("type", "access")
                .verifyWith(properties.secretKey())
                .build();
    }

    public MemberIdentity authorize(String token) {
        try {
            return generateIdentity(token);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new MemberIdentityException(exception);
        }
    }

    private MemberIdentity generateIdentity(String token) throws JwtException, IllegalArgumentException {
        Jws<Claims> claimsJws = jwtParser.parseSignedClaims(token);
        Claims payload = claimsJws.getPayload();
        String subject = payload.getSubject();

        return new MemberIdentity(Long.parseLong(subject));
    }
}
