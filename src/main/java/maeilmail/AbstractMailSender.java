package maeilmail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractMailSender<T> {

    private static final int MAIL_SENDER_RATE_MILLISECONDS = 500;
    protected static final String FROM_EMAIL = "maeil-mail <maeil-mail-noreply@maeil-mail.site>";

    protected final JavaMailSender javaMailSender;

    @Async
    public void sendMail(T message) {
        try {
            logSending(message);
            MimeMessage mimeMessage = createMimeMessage(message);
            javaMailSender.send(mimeMessage);
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

    protected abstract MimeMessage createMimeMessage(T message) throws MessagingException;

    protected abstract void handleSuccess(T message);

    protected abstract void handleFailure(T message);
}
