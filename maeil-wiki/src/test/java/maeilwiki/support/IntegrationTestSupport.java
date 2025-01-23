package maeilwiki.support;

import maeilwiki.WikiConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(classes = WikiConfiguration.class)
@Import(IntegrationTestSupport.TestConfig.class)
public abstract class IntegrationTestSupport {

    @EnableJpaAuditing
    @TestConfiguration
    public static class TestConfig {
    }
}
