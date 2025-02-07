package maeilwiki.member.api;

import lombok.RequiredArgsConstructor;
import maeilwiki.member.application.MemberRefreshRequest;
import maeilwiki.member.application.MemberRequest;
import maeilwiki.member.application.MemberService;
import maeilwiki.member.application.MemberTokenResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class MemberApi {

    private final MemberService memberService;
    private final MemberIdentityCookieHelper cookieHelper;

    @PostMapping("/member")
    public ResponseEntity<Void> createMember(@RequestBody MemberRequest request) {
        MemberTokenResponse response = memberService.apply(request);

        return generateTokenCookieIncludeResponse(response);
    }

    @PostMapping("/member/refresh")
    public ResponseEntity<Void> refresh(@CookieValue String refreshToken) {
        MemberRefreshRequest request = new MemberRefreshRequest(refreshToken);
        MemberTokenResponse response = memberService.refresh(request);

        return generateTokenCookieIncludeResponse(response);
    }

    private ResponseEntity<Void> generateTokenCookieIncludeResponse(MemberTokenResponse response) {
        String accessTokenCookie = cookieHelper.generateAccessTokenCookie(response.accessToken());
        String refreshTokenCookie = cookieHelper.generateRefreshTokenCookie(response.refreshToken(), "/member/refresh");

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie)
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie)
                .build();
    }
}
