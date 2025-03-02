package maeilwiki.member.infra.github;

import lombok.RequiredArgsConstructor;
import maeilwiki.member.domain.Member;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GithubMemberFactory {

    private final GithubClient githubClient;

    public Member create(String accessToken) {
        GithubMember githubMember = githubClient.getGithubMember(accessToken);
        return githubMember.toMember();
    }
}
