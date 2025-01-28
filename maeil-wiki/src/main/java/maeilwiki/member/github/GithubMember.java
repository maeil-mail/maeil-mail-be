package maeilwiki.member.github;

import maeilwiki.member.domain.Member;
import maeilwiki.member.domain.Provider;

public record GithubMember(
        Long id,
        String name,
        String login,
        String avatarUrl
) {

    public Member toMember() {
        String actualName = generateName();
        String providerId = generateProviderId(id);

        return new Member(actualName, providerId, Provider.GITHUB, avatarUrl);
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
