package maeilmail.support;

public class RateLimitExceededException extends IllegalStateException {

    public RateLimitExceededException() {
        super("처리율 제한을 초과했습니다.");
    }
}
