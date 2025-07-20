package maeilwiki.support;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
public abstract class IntegrationTestSupport {
}
