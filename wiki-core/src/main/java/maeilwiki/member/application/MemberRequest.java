package maeilwiki.member.application;

import jakarta.validation.constraints.NotBlank;

public record MemberRequest(
        @NotBlank(message = "OAuth Access Token은 필수입니다.")
        String oauthAccessToken) {
}
