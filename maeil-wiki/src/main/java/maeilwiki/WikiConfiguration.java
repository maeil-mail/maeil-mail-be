package maeilwiki;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"maeilwiki", "maeilsupport"})
@EnableAutoConfiguration
public class WikiConfiguration {
}
