package maeilmail.support;

import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;

import maeilmail.bulksend.QuestionSender;
import maeilmail.mail.MailSender;
import maeilmail.subscribe.command.application.VerifySubscribeService;
import org.hibernate.cfg.AvailableSettings;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Import(IntegrationTestSupport.TestConfig.class)
public abstract class IntegrationTestSupport {

    @Autowired
    private CacheManager cacheManager;

    @AfterEach
    void tearDown() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }

    @TestConfiguration
    public static class TestConfig {

        @Bean
        public MailSender emailSender() {
            MailSender mailSender = mock(MailSender.class);
            willDoNothing()
                    .given(mailSender)
                    .sendMail(any());

            return mailSender;
        }

        @Bean
        public QuestionSender questionSender() {
            QuestionSender questionSender = mock(QuestionSender.class);
            willDoNothing()
                    .given(questionSender)
                    .sendMail(any());

            return questionSender;
        }

        @Bean
        public VerifySubscribeService verifySubscribeService() {
            VerifySubscribeService verifySubscribeService = mock(VerifySubscribeService.class);
            willDoNothing()
                    .given(verifySubscribeService)
                    .verify(any(), any());

            return verifySubscribeService;
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
