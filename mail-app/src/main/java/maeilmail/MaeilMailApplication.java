package maeilmail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ConfigurationPropertiesScan(basePackages = {BasePackages.MAEIL_MAIL, BasePackages.MAEIL_WIKI})
@SpringBootApplication(
        scanBasePackages = {BasePackages.MAEIL_MAIL, BasePackages.MAEIL_WIKI},
        exclude = HibernateJpaAutoConfiguration.class
)
public class MaeilMailApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaeilMailApplication.class, args);
    }
}
