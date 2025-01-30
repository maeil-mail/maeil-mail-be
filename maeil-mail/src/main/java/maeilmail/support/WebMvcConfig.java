package maeilmail.support;

import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilwiki.member.api.MemberIdentityArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
class WebMvcConfig implements WebMvcConfigurer {

    private final MemberIdentityArgumentResolver identityArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(identityArgumentResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.PATCH.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.OPTIONS.name()
                )
                .allowedOrigins(
                        "http://localhost:5173",
                        "http://localhost:3000",
                        "https://maeil-mail-admin.vercel.app",
                        "https://maeil-mail.vercel.app",
                        "https://maeil-mail-fe.vercel.app",
                        "https://www.maeil-mail.kr",
                        "https://maeil-mail.kr"
                );
    }
}
