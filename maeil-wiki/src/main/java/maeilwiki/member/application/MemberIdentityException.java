package maeilwiki.member.application;

public class MemberIdentityException extends RuntimeException {

    private static final String IDENTITY_EXCEPTION_MESSAGE = "유효한 토큰이 존재하지 않습니다.";

    private final boolean isExpired;

    public MemberIdentityException() {
        super(IDENTITY_EXCEPTION_MESSAGE);
        this.isExpired = false;
    }

    public MemberIdentityException(Throwable cause) {
        super(IDENTITY_EXCEPTION_MESSAGE, cause);
        this.isExpired = false;
    }

    public MemberIdentityException(Throwable cause, boolean isExpired) {
        super(IDENTITY_EXCEPTION_MESSAGE, cause);
        this.isExpired = isExpired;
    }

    public boolean isExpired() {
        return isExpired;
    }
}
