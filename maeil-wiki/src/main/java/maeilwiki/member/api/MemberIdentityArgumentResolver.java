package maeilwiki.member.api;

import static maeilwiki.member.api.MemberIdentityCookieType.ACCESS_TOKEN;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import maeilwiki.member.application.MemberIdentity;
import maeilwiki.member.application.MemberIdentityException;
import maeilwiki.member.infra.MemberTokenAuthorizer;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class MemberIdentityArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberTokenAuthorizer authorizer;
    private final MemberIdentityCookieHelper cookieHelper;

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

        Cookie[] cookies = request.getCookies();
        String accessToken = cookieHelper.getCookieByName(cookies, ACCESS_TOKEN);
        if (accessToken == null) {
            throw new MemberIdentityException();
        }

        return authorizer.authorize(accessToken);
    }
}
