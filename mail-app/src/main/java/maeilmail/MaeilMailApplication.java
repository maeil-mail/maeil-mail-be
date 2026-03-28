package maeilmail;

import static maeilmail.BasePackages.MAEIL_MAIL;
import static maeilmail.BasePackages.MAEIL_WIKI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableRetry
@ConfigurationPropertiesScan(basePackages = {MAEIL_MAIL, MAEIL_WIKI})
@SpringBootApplication(
        scanBasePackages = {MAEIL_MAIL, MAEIL_WIKI},
        exclude = HibernateJpaAutoConfiguration.class
)
public class MaeilMailApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaeilMailApplication.class, args);
    }
}
