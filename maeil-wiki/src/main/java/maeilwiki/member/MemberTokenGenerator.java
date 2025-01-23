package maeilwiki.member;

import java.time.Duration;
import java.util.Date;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class MemberTokenGenerator {

    private final MemberTokenProperties properties;

    public String generateAccessToken(Member member) {
        Date date = new Date();
        Duration accessExpTime = properties.accessExpTime();
        Date expiration = Date.from(date.toInstant().plus(accessExpTime));

        return Jwts.builder()
                .subject(member.getId().toString())
                .issuedAt(date)
                .expiration(expiration)
                .claim("picture", member.getProfileImageUrl())
                .claim("name", member.getName())
                .signWith(properties.secretKey())
                .compact();
    }

    public String generateRefreshToken() {
        Date date = new Date();
        Duration refreshExpTime = properties.refreshExpTime();
        Date expiration = Date.from(date.toInstant().plus(refreshExpTime));

        return Jwts.builder()
                .issuedAt(date)
                .expiration(expiration)
                .signWith(properties.secretKey())
                .compact();
    }
}
