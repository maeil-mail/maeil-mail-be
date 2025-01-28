package maeilwiki.member;

public class IdentityException extends RuntimeException {

    private static final String IDENTITY_EXCEPTION_MESSAGE = "유효한 토큰이 존재하지 않습니다.";

    public IdentityException() {
        super(IDENTITY_EXCEPTION_MESSAGE);
    }

    public IdentityException(Throwable cause) {
        super(IDENTITY_EXCEPTION_MESSAGE, cause);
    }
}
