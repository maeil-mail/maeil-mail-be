package maeilbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ConfigurationPropertiesScan(basePackages = {"maeilmail", "maeilbatch"})
@SpringBootApplication(
        scanBasePackages = {"maeilmail", "maeilbatch"}
)
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {"maeilmail", "maeilbatch"})
@EntityScan(basePackages = {"maeilmail", "maeilbatch"})
class MaeilBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaeilBatchApplication.class, args);
    }
}
