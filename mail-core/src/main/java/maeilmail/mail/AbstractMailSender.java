package maeilmail.mail;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.support.DistributedRateLimitSupport;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractMailSender<T extends MailMessage> {

    private static final Duration WAIT_TIMEOUT = Duration.of(2, ChronoUnit.SECONDS);

    private final JavaMailSender javaMailSender;
    private final MimeMessageCustomizer mimeMessageCustomizer;
    private final DistributedRateLimitSupport limiter;

    public void sendMailSync(T message) {
        sendMail(message);
    }

    @Async
    public void sendMail(T message) {
        try {
            limiter.consumeBlocking(WAIT_TIMEOUT);
            logSending(message);
            MimeMessage emptyMimeMessage = javaMailSender.createMimeMessage();
            MimeMessage targetMimeMessage = mimeMessageCustomizer.customize(emptyMimeMessage, message);
            javaMailSender.send(targetMimeMessage);
            handleSuccess(message);
        } catch (MessagingException | MailException e) {
            log.error("메일 전송 실패: {}", e.getMessage(), e);
            handleFailure(message);
        } catch (Exception e) {
            log.error("예기치 않은 오류 발생: {}", e.getMessage(), e);
            handleFailure(message);
        }
    }

    protected abstract void logSending(T message);

    protected abstract void handleSuccess(T message);

    protected abstract void handleFailure(T message);
}
