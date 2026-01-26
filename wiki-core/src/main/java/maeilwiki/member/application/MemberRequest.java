package maeilwiki.member.application;

import jakarta.validation.constraints.NotBlank;

public record MemberRequest(
        String oauthAccessToken) {
}
