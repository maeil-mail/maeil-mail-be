package maeilwiki.member.infra;

import java.util.function.Function;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import maeilwiki.member.application.MemberIdentityException;
import org.springframework.stereotype.Component;

/**
 * jjwt 파싱 예외를 핸들링합니다.
 */
@Component
class MemberTokenExceptionHandler {

    public <T, R> R handle(Function<T, R> fn, T argument) throws MemberIdentityException {
        try {
            return fn.apply(argument);
        } catch (ExpiredJwtException expiredJwtException) {
            throw new MemberIdentityException(expiredJwtException, true);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new MemberIdentityException(exception);
        }
    }
}
