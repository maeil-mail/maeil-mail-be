package maeilbatch.config;

import maeilbatch.SmtpConnectionPool;
import maeilbatch.SmtpConnectionPoolProxy;
import maeilbatch.SmtpConnectionProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public class MailSenderBeanPostProcessor implements BeanPostProcessor, DisposableBean {

    private SmtpConnectionPoolProxy pooledMailSender;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!"mailSender".equals(beanName) || !(bean instanceof JavaMailSenderImpl delegate)) {
            return bean;
        }

        SmtpConnectionProperties settings = SmtpConnectionProperties.from(delegate);
        SmtpConnectionPool connectionPool = new SmtpConnectionPool(settings);
        SmtpConnectionPoolProxy pooledMailSender = new SmtpConnectionPoolProxy(delegate, connectionPool);
        pooledMailSender.testConnection();

        this.pooledMailSender = pooledMailSender;

        return this.pooledMailSender;
    }

    @Override
    public void destroy() {
        if (pooledMailSender != null) {
            pooledMailSender.close();
        }
    }
}
