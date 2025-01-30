package maeilwiki.member.infra;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "token")
public record MemberTokenProperties(SecretKey secretKey, Duration accessExpTime, Duration refreshExpTime) {

    @ConstructorBinding
    public MemberTokenProperties(String secretKey, Duration accessExpTime, Duration refreshExpTime) {
        this(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), accessExpTime, refreshExpTime);
    }
}
