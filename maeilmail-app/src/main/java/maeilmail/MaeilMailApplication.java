package maeilmail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ConfigurationPropertiesScan(basePackages = "maeilwiki")
@SpringBootApplication(scanBasePackages = {"maeilmail", "maeilwiki"})
public class MaeilMailApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaeilMailApplication.class, args);
    }
}
