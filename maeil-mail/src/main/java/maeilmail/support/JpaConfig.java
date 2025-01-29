package maeilmail.support;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = {"maeilmail", "maeilwiki"})
@EnableJpaRepositories(basePackages = {"maeilmail", "maeilwiki"})
public class JpaConfig {
}
