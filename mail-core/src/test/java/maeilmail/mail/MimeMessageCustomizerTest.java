package maeilmail.mail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

class MimeMessageCustomizerTest {

    @DisplayName("메일의 MimeMessage를 생성한다.")
    @Test
    void customize() throws MessagingException {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("localhost");
        javaMailSender.setPort(25);
        javaMailSender.setSession(Session.getDefaultInstance(new Properties()));

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MailMessage message = new SimpleMailMessage("to", "subject", "text", "type");

        MimeMessageCustomizer customizer = new MimeMessageCustomizer();

        MimeMessage result = customizer.customize(mimeMessage, message);

        assertThat(result.getSubject()).isEqualTo("[매일메일] subject");
    }
}
