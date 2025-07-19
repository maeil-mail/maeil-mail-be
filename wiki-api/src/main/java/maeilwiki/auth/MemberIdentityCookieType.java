package maeilwiki.auth;

import lombok.Getter;

@Getter
enum MemberIdentityCookieType {

    ACCESS_TOKEN("accessToken"), REFRESH_TOKEN("refreshToken");

    private final String name;

    MemberIdentityCookieType(String name) {
        this.name = name;
    }
}
