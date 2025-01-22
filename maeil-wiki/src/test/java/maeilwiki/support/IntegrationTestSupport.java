package maeilwiki.support;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
public abstract class IntegrationTestSupport {
}
