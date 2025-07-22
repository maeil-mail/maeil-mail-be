package maeilwiki.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import maeilwiki.member.infra.github.GithubClient;
import maeilwiki.member.infra.github.GithubMember;
import maeilwiki.support.extension.DatabaseCleanerExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@ExtendWith(DatabaseCleanerExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Import(IntegrationTestSupport.TestConfig.class)
public abstract class IntegrationTestSupport {

    @EnableJpaAuditing
    @TestConfiguration
    static class TestConfig {

        @Bean(name = "wikiCoreIntegrationTestJpaQueryFactory")
        public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
            return new JPAQueryFactory(JPQLTemplates.DEFAULT, entityManager);
        }

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
