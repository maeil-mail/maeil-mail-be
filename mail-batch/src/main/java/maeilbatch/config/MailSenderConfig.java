package maeilbatch.config;

import maeilbatch.smtp.MailSenderBeanPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailSenderConfig {

    @Bean
    public BeanPostProcessor connectionPoolJavaMailSenderBeanPostProcessor() {
        return new MailSenderBeanPostProcessor();
    }
}
