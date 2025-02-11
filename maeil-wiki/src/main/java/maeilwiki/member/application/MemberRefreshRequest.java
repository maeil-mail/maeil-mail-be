package maeilwiki.member.application;

public record MemberRefreshRequest(String refreshToken) {

    public MemberRefreshRequest {
        if (refreshToken == null) {
            throw new MemberIdentityException();
        }
    }
}
