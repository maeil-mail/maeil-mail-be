package maeilwiki.member.infra.github;

import maeilwiki.support.IntegrationTestSupport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 참고: https://docs.github.com/ko/rest/users/users?apiVersion=2022-11-28
 */
class GithubClientLearningTest extends IntegrationTestSupport {

    @Autowired
    private GithubClient githubClient;

    @Test
    @Disabled
    @DisplayName("사용자 정보 조회 API 학습 테스트")
    void learningTest() {
        String accessToken = "atom"; // GITHUB developer setting에서 테스트 용으로 발급
        GithubMember githubMember = githubClient.getGithubMember(accessToken);
    }
}
