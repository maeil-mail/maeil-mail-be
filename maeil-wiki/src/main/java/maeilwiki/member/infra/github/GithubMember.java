package maeilwiki.member.infra.github;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import maeilwiki.member.domain.Member;
import maeilwiki.member.domain.Provider;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record GithubMember(
        Long id,
        String name,
        String login,
        String avatarUrl
) {

    public Member toMember() {
        String actualName = generateName();
        String providerId = generateProviderId(id);

        return new Member(actualName, providerId, Provider.GITHUB, avatarUrl, "github.com");
    }

    private String generateName() {
        if (name == null) {
            return login;
        }

        return name;
    }

    private String generateProviderId(Long id) {
        String providerIdPrefix = Provider.GITHUB.getProviderIdPrefix();

        return String.format(providerIdPrefix, id);
    }
}
