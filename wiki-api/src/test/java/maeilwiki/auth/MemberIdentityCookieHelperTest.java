package maeilwiki.auth;

import static maeilwiki.auth.MemberIdentityCookieType.ACCESS_TOKEN;
import static maeilwiki.auth.MemberIdentityCookieType.REFRESH_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import jakarta.servlet.http.Cookie;
import maeilwiki.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

class MemberIdentityCookieHelperTest extends IntegrationTestSupport {

    @Autowired
    private MemberIdentityCookieHelper helper;

    @Test
    @DisplayName("주어진 쿠키 목록에서 원하는 쿠키를 조회할 수 있다.")
    void getCookieById() {
        String expectedValue = "accessToken";
        Cookie accessCookie = new Cookie(ACCESS_TOKEN.getName(), expectedValue);
        Cookie refreshCookie = new Cookie(REFRESH_TOKEN.getName(), "refreshToken");

        Cookie[] cookies = {accessCookie, refreshCookie};

        String result = helper.getCookieByName(cookies, ACCESS_TOKEN);

        assertThat(result).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("쿠키를 찾을 수 없는 경우, null을 반환한다.")
    void cookieNotFound() {
        Cookie accessCookie = new Cookie(ACCESS_TOKEN.getName(), "1234");
        Cookie[] cookies = {accessCookie};

        String result = helper.getCookieByName(cookies, REFRESH_TOKEN);

        assertThat(result).isNull();
    }

    @ParameterizedTest
    @MethodSource(value = "generateInvalidCookies")
    @DisplayName("쿠키 배열이 비정상 케이스인 경우, null을 반환한다.")
    void invalidCookies(Cookie[] cookies) {
        String result = helper.getCookieByName(cookies, ACCESS_TOKEN);

        assertThat(result).isNull();
    }

    public static Stream<Arguments> generateInvalidCookies() {
        return Stream.of(Arguments.of((Object) new Cookie[]{}), Arguments.of((Object) null));
    }
}
