package maeilwiki.member.infra.github;

import java.time.Duration;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GithubClient {

    private static final int CONNECT_TIMEOUT = 3;
    private static final int READ_TIMEOUT = 10;

    private final RestClient restClient;

    public GithubClient() {
        this.restClient = createRestClient(RestClient.builder());
    }

    private RestClient createRestClient(RestClient.Builder restClientBuilder) {
        return restClientBuilder.requestFactory(clientHttpRequestFactory()).build();
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT))
                .withReadTimeout(Duration.ofSeconds(READ_TIMEOUT));

        return ClientHttpRequestFactories.get(settings);
    }

    public GithubMember getGithubMember(String accessToken) {
        return restClient.get()
                .uri("https://api.github.com/user")
                .header("X-Github-Api-Version", "2022-11-28")
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, getGithubMemberErrorHandler())
                .body(GithubMember.class);
    }

    private RestClient.ResponseSpec.ErrorHandler getGithubMemberErrorHandler() {
        return (request, response) -> {
            throw new IllegalArgumentException("잘못된 엑세스 토큰입니다.");
        };
    }
}
