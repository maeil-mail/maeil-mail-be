package maeilmail.support;

import maeilmail.bulksend.sender.QuestionSender;
import maeilmail.bulksend.sender.WeeklyQuestionSender;
import maeilmail.mail.MailSender;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public abstract class IntegrationTestSupport {

    @MockBean
    protected MailSender mailSender;

    @MockBean
    protected QuestionSender questionSender;

    @MockBean
    protected WeeklyQuestionSender weeklyQuestionSender;
}
