package maeilmail.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractMailSender<T> {

    private static final int MAIL_SENDER_RATE_MILLISECONDS = 500;

    protected final JavaMailSender javaMailSender;
    protected final MimeMessageCustomizer<T> mimeMessageCustomizer;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendMailWithTransaction(T message) {
        sendMail(message);
    }

    @Async
    public void sendMail(T message) {
        try {
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
        } finally {
            try {
                Thread.sleep(MAIL_SENDER_RATE_MILLISECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    protected abstract void logSending(T message);

    protected abstract void handleSuccess(T message);

    protected abstract void handleFailure(T message);
}
