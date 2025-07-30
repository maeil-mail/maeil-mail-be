package maeilmail.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component("emailSender")
public class MailSender extends AbstractMailSender<MailMessage> {

    private final MailEventRepository mailEventRepository;

    public MailSender(
            JavaMailSender javaMailSender,
            MailMimeMessageCreator mimeMessageCreator,
            MailEventRepository mailEventRepository
    ) {
        super(javaMailSender, mimeMessageCreator);
        this.mailEventRepository = mailEventRepository;
    }

    @Override
    protected void logSending(MailMessage message) {
        log.info("메일을 전송합니다. email = {} subject = {} type = {}", message.to(), message.subject(), message.type());
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
