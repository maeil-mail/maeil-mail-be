package maeilwiki.support;

import maeilwiki.WikiConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(classes = WikiConfiguration.class)
public abstract class IntegrationTestSupport {
}
