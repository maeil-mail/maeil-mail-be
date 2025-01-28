package maeilwiki.member;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component
class MemberTokenAuthorizer {

    private final JwtParser jwtParser;

    public MemberTokenAuthorizer(MemberTokenProperties properties) {
        this.jwtParser = Jwts.parser()
                .require("type", "access")
                .verifyWith(properties.secretKey())
                .build();
    }

    public Identity authorize(String token) {
        try {
            return generateIdentity(token);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new IdentityException(exception);
        }
    }

    private Identity generateIdentity(String token) throws JwtException, IllegalArgumentException {
        Jws<Claims> claimsJws = jwtParser.parseSignedClaims(token);
        Claims payload = claimsJws.getPayload();
        String subject = payload.getSubject();

        return new Identity(Long.parseLong(subject));
    }
}
