package maeilbatch.config;

import java.util.Properties;
import maeilbatch.ConnectionPoolJavaMailSender;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class JavaMailSenderConfig {

    @Bean
    public JavaMailSenderImpl delegateJavaMailSender(MailProperties mailProperties) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(mailProperties.getHost());
        sender.setPort(mailProperties.getPort());
        sender.setUsername(mailProperties.getUsername());
        sender.setPassword(mailProperties.getPassword());
        sender.setProtocol(mailProperties.getProtocol());
        if (mailProperties.getDefaultEncoding() != null) {
            sender.setDefaultEncoding(mailProperties.getDefaultEncoding().name());
        }

        Properties properties = new Properties();
        properties.putAll(mailProperties.getProperties());
        sender.setJavaMailProperties(properties);
        return sender;
    }

    @Bean(destroyMethod = "close")
    @Primary
    public JavaMailSender connectionPoolJavaMailSender(JavaMailSenderImpl delegateJavaMailSender) {
        return new ConnectionPoolJavaMailSender(delegateJavaMailSender);
    }
}
