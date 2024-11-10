package maeilmail.support;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import maeilmail.mail.MailSender;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
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
    }
}
