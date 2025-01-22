package maeilmail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"maeilmail", "maeilwiki", "maeilsupport"})
@EntityScan(basePackages = {"maeilmail", "maeilwiki"})
@EnableJpaRepositories(basePackages = {"maeilmail", "maeilwiki"})
public class MaeilMailApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaeilMailApplication.class, args);
    }
}
