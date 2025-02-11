package maeilmail.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Value("${admin.secret}")
    private String adminSecret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String[] authTokens = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ");
        String authType = authTokens[0];
        String secret = authTokens[1];

        validateAuthType(authType);
        validateIdentity(secret);

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    private void validateAuthType(String authType) {
        if (!("Basic".equals(authType))) {
            throw new IllegalArgumentException("지원하지 않는 인증 타입입니다.");
        }
    }

    private void validateIdentity(String secret) {
        if (!(adminSecret.equals(secret))) {
            throw new IllegalArgumentException("어드민 경로 접근 권한이 없습니다.");
        }
    }
}
