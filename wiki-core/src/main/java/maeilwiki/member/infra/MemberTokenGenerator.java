package maeilwiki.member.infra;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import maeilwiki.member.domain.Member;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberTokenGenerator {

    private final MemberTokenProperties properties;

    public String generateAccessToken(Member member) {
        Date date = new Date();
        Duration accessExpTime = properties.accessExpTime();
        Date expiration = Date.from(date.toInstant().plus(accessExpTime));

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(member.getId().toString())
                .issuedAt(date)
                .expiration(expiration)
                .claim("picture", member.getProfileImageUrl())
                .claim("name", member.getName())
                .claim("type", "access")
                .signWith(properties.secretKey())
                .compact();
    }

    public String generateRefreshToken() {
        Date date = new Date();
        Duration refreshExpTime = properties.refreshExpTime();
        Date expiration = Date.from(date.toInstant().plus(refreshExpTime));

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuedAt(date)
                .expiration(expiration)
                .claim("type", "refresh")
                .signWith(properties.secretKey())
                .compact();
    }
}
