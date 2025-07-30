package maeilmail.bulksend.sender;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

class QuestionMimeMessageCreatorTest {

    @DisplayName("일간질문지의 MimeMessage를 생성한다.")
    @Test
    void createMimeMessage() throws MessagingException {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("localhost");
        javaMailSender.setPort(25);
        javaMailSender.setSession(Session.getDefaultInstance(new Properties()));

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        Subscribe subscribe = new Subscribe("test@test.com", QuestionCategory.BACKEND, SubscribeFrequency.DAILY);
        Question question = new Question("test1", "content", QuestionCategory.BACKEND);
        SubscribeQuestionMessage message = new SubscribeQuestionMessage(subscribe, question, "subject", "text");

        QuestionMimeMessageCreator creator = new QuestionMimeMessageCreator();

        MimeMessage result = creator.createMimeMessage(mimeMessage, message);

        assertThat(result.getSubject()).isEqualTo("[매일메일] subject");
    }
}
