package maeilwiki.member.api;

import static maeilwiki.member.api.MemberIdentityCookieType.ACCESS_TOKEN;
import static maeilwiki.member.api.MemberIdentityCookieType.REFRESH_TOKEN;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import maeilwiki.member.infra.MemberTokenProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberIdentityCookieHelper {

    private final MemberTokenProperties properties;

    @Value("${domain}")
    private String serverDomain;

    public String generateAccessTokenCookie(String value) {
        return ResponseCookie.from(ACCESS_TOKEN.getName(), value)
                .domain(serverDomain)
                .path("/")
                .maxAge(properties.accessExpTime())
                .sameSite("Strict")
                .httpOnly(true)
                .secure(true)
                .build()
                .toString();
    }

    public String generateRefreshTokenCookie(String value, String path) {
        return ResponseCookie.from(REFRESH_TOKEN.getName(), value)
                .domain(serverDomain)
                .path(path)
                .maxAge(properties.refreshExpTime())
                .sameSite("Strict")
                .httpOnly(true)
                .secure(true)
                .build()
                .toString();
    }

    public String getCookieByName(Cookie[] cookies, MemberIdentityCookieType cookieType) {
        if (cookies == null || cookies.length == 0) {
            return null;
        }

        String target = null;
        for (Cookie cookie : cookies) {
            String name = cookie.getName();
            if (name.equals(cookieType.getName())) {
                target = cookie.getValue();
            }
        }

        return target;
    }
}
