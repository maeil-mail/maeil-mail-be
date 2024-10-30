package maeilmail.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component(value = "emailSender")
@RequiredArgsConstructor
public class MailSender {

    private static final String FROM_EMAIL = "maeil-mail <maeil-mail-noreply@maeil-mail.site>";

    private final JavaMailSender javaMailSender;
    private final MailEventRepository mailEventRepository;

    @Async
    public void sendMail(MailMessage message) {

        try {
            log.info("메일을 전송합니다. email = {} question = {} type = {}", message.to(), message.subject(), message.type());
            MimeMessage mimeMessage = convertToMime(message);
            javaMailSender.send(mimeMessage);
            mailEventRepository.save(MailEvent.success(message.to(), message.type()));
        } catch (MessagingException e) {
            mailEventRepository.save(MailEvent.fail(message.to(), message.type()));
            log.info("메일 전송 실패 = {}", e.getMessage());
        }
    }

    private MimeMessage convertToMime(MailMessage message) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        tryAppendOpenEventTrace(message, mimeMessage);
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        mimeMessageHelper.setFrom(FROM_EMAIL);
        mimeMessageHelper.setTo(message.to());
        mimeMessageHelper.setSubject(message.subject());
        mimeMessageHelper.setText(message.text(), true);
        return mimeMessage;
    }

    private void tryAppendOpenEventTrace(MailMessage message, MimeMessage mimeMessage) throws MessagingException {
        if ("question".equals(message.type())) {
            mimeMessage.setHeader("X-SES-CONFIGURATION-SET", "my-first-configuration-set");
            mimeMessage.setHeader("X-SES-MESSAGE_TAGS", "mail-open");
        }
    }
}
