package maeilmail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"maeilmail", "maeilwiki"})
public class MaeilMailApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaeilMailApplication.class, args);
    }
}
