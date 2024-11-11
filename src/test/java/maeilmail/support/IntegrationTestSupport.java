package maeilmail.support;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import maeilmail.mail.MailSender;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@Import(IntegrationTestSupport.TestConfig.class)
public abstract class IntegrationTestSupport {

    @TestConfiguration
    public static class TestConfig {

        @Bean
        public MailSender mailSender() {
            MailSender mailSender = mock(MailSender.class);
            doNothing()
                    .when(mailSender)
                    .sendMail(any());

            return mailSender;
        }

        @Bean
        public QueryCountTester queryCountTester() {
            return new QueryCountTester();
        }

        @Bean
        public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
            QueryCountInspector queryCountInspector = new QueryCountInspector(queryCountTester());

            return hibernateProperties ->
                    hibernateProperties.put(AvailableSettings.STATEMENT_INSPECTOR, queryCountInspector);
        }
    }
}
