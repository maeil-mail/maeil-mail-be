package maeilmail.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final String ADMIN_AUTH_FAIL_MESSAGE = "어드민 경로 접근 권한이 없습니다.";

    @Value("${admin.secret}")
    private String adminSecret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (isPreflight(request)) {
            return true;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null) {
            throw new IllegalArgumentException(ADMIN_AUTH_FAIL_MESSAGE);
        }

        String[] authTokens = header.split(" ");
        String authType = authTokens[0];
        String secret = authTokens[1];

        validateAuthType(authType);
        validateIdentity(secret);

        return true;
    }

    private boolean isPreflight(HttpServletRequest request) {
        return "OPTIONS".equals(request.getMethod());
    }

    private void validateAuthType(String authType) {
        if (!("Basic".equals(authType))) {
            throw new IllegalArgumentException("지원하지 않는 인증 타입입니다.");
        }
    }

    private void validateIdentity(String secret) {
        if (!(adminSecret.equals(secret))) {
            throw new IllegalArgumentException(ADMIN_AUTH_FAIL_MESSAGE);
        }
    }
}
