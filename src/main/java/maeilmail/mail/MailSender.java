package maeilmail.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import maeilmail.AbstractMailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component("emailSender")
public class MailSender extends AbstractMailSender<MailMessage> {

    private final MailEventRepository mailEventRepository;

    public MailSender(JavaMailSender javaMailSender, MailEventRepository mailEventRepository) {
        super(javaMailSender);
        this.mailEventRepository = mailEventRepository;
    }

    @Override
    protected void logSending(MailMessage message) {
        log.info("메일을 전송합니다. email = {} subject = {} type = {}", message.to(), message.subject(), message.type());
    }

    @Override
    protected MimeMessage createMimeMessage(MailMessage message) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setFrom(FROM_EMAIL);
        helper.setTo(message.to());
        helper.setSubject("[매일메일] " + message.subject());
        helper.setText(message.text(), true);
        return mimeMessage;
    }

    @Override
    protected void handleSuccess(MailMessage message) {
        mailEventRepository.save(MailEvent.success(message.to(), message.type()));
    }

    @Override
    protected void handleFailure(MailMessage message) {
        mailEventRepository.save(MailEvent.fail(message.to(), message.type()));
    }
}
