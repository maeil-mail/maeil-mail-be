package maeilmail.support;

import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import maeilmail.bulksend.sender.QuestionSender;
import maeilmail.bulksend.sender.WeeklyQuestionSender;
import maeilmail.mail.MailSender;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(IntegrationTestSupport.TestConfig.class)
public abstract class IntegrationTestSupport {

    @MockBean
    protected MailSender mailSender;

    @MockBean
    protected QuestionSender questionSender;

    @MockBean
    protected WeeklyQuestionSender weeklyQuestionSender;

    @TestConfiguration
    public static class TestConfig {

        @Bean(name = "adminIntegrationTestJpaQueryFactory")
        public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
            return new JPAQueryFactory(JPQLTemplates.DEFAULT, entityManager);
        }
    }
}
