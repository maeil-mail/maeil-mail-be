package maeilwiki.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import maeilwiki.member.github.GithubClient;
import maeilwiki.member.github.GithubMember;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Import(IntegrationTestSupport.TestConfig.class)
public abstract class IntegrationTestSupport {

    @TestConfiguration
    static class TestConfig {

        @Bean
        public GithubClient githubClient() {
            GithubClient githubClient = mock(GithubClient.class);
            GithubMember githubMember = new GithubMember(1234L, "atom", "atom", "www.naver.com");
            when(githubClient.getGithubMember(any()))
                    .thenReturn(githubMember);

            return githubClient;
        }
    }
}
