package maeilmail.admin.api;

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
        if (header == null || !header.startsWith("Basic ")) {
            throw new IllegalArgumentException(ADMIN_AUTH_FAIL_MESSAGE);
        }

        String secret = header.substring("Basic ".length());
        if (!adminSecret.equals(secret)) {
            throw new IllegalArgumentException(ADMIN_AUTH_FAIL_MESSAGE);
        }
        return true;
    }

    private boolean isPreflight(HttpServletRequest request) {
        return "OPTIONS".equals(request.getMethod());
    }
}
