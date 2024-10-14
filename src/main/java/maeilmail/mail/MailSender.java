package maeilmail.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component(value = "emailSender")
@RequiredArgsConstructor
public class MailSender {

    private static final String FROM_EMAIL = "maeil-mail-noreply@maeil-mail.site";

    private final JavaMailSender javaMailSender;
    private final MailEventRepository mailEventRepository;

    public void sendMail(MailMessage message) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setFrom(FROM_EMAIL);
            mimeMessageHelper.setTo(message.to());
            mimeMessageHelper.setSubject(message.subject());
            mimeMessageHelper.setText(message.text(), true);
            javaMailSender.send(mimeMessage);
            mailEventRepository.save(MailEvent.success(message.to(), message.type()));
        } catch (MessagingException e) {
            mailEventRepository.save(MailEvent.fail(message.to(), message.type()));
            log.info("메일 전송 실패 = {}", e.getMessage());
        }
    }
}
