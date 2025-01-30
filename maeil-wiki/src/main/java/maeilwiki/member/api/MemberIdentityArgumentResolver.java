package maeilwiki.member.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import maeilwiki.member.application.MemberIdentity;
import maeilwiki.member.application.MemberIdentityException;
import maeilwiki.member.infra.MemberTokenAuthorizer;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class MemberIdentityArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberTokenAuthorizer authorizer;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return MemberIdentity.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new MemberIdentityException();
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String[] tokens = authHeader.split(" ");
        String authType = tokens[0];
        String token = tokens[1];
        validateBearerAuth(authType);

        return authorizer.authorize(token);
    }

    private void validateBearerAuth(String authType) {
        if (!"Bearer".equals(authType)) {
            throw new MemberIdentityException();
        }
    }
}
