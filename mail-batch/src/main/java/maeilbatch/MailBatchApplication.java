package maeilbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan(basePackages = {"maeilmail", "maeilbatch"})
@SpringBootApplication(
        scanBasePackages = {"maeilmail", "maeilbatch"}
)
class MailBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailBatchApplication.class, args);
    }
}
