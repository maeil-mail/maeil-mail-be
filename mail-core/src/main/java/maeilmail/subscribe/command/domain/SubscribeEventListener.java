package maeilmail.subscribe.command.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.mail.MailMessage;
import maeilmail.mail.MailSender;
import maeilmail.subscribe.view.SubscribeWelcomeView;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribeEventListener {
    private final MailSender mailSender;
    private final SubscribeWelcomeView welcomeView;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendWelcomeMail(SubscribeCreatedEvent event) {
        String subject = "앞으로 면접 질문을 보내드릴게요.";
        String text = welcomeView.render(new HashMap<>());
        MailMessage mailMessage = new MailMessage(event.email(), subject, text, welcomeView.getType());
        mailSender.sendMail(mailMessage);
    }
}
