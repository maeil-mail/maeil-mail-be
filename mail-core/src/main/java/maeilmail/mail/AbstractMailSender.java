package maeilmail.mail;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.support.DistributedRateLimitSupport;
import maeilmail.support.RateLimitExceededException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractMailSender<T extends MailMessage> {

    private static final Duration WAIT_TIMEOUT = Duration.of(2, ChronoUnit.SECONDS);
    private static final long BACKOFF_DELAY = 500L;

    private final JavaMailSender javaMailSender;
    private final MimeMessageCustomizer mimeMessageCustomizer;
    private final DistributedRateLimitSupport limiter;

    @Retryable(
            maxAttempts = 2,
            backoff = @Backoff(delay = BACKOFF_DELAY, multiplier = 2),
            retryFor = RetryableMailException.class,
            recover = "recover"
    )
    public void sendMailSync(T message) {
        doSend(message);
    }

    @Async
    @Retryable(
            maxAttempts = 2,
            backoff = @Backoff(delay = BACKOFF_DELAY, multiplier = 2),
            retryFor = RetryableMailException.class,
            recover = "recover"
    )
    public void sendMail(T message) {
        doSend(message);
    }

    @Recover
    protected void recover(Exception e, T message) {
        log.error("최종 메일 전송 실패: {}", e.getMessage());
        handleFailure(message);
    }

    private void doSend(T message) {
        try {
            limiter.consumeBlocking(WAIT_TIMEOUT);
            logSending(message);
            MimeMessage emptyMimeMessage = javaMailSender.createMimeMessage();
            MimeMessage targetMimeMessage = mimeMessageCustomizer.customize(emptyMimeMessage, message);
            javaMailSender.send(targetMimeMessage);
            handleSuccess(message);
        } catch (RateLimitExceededException e) {
            logMailSendingFailed(e);
            throw new RetryableMailException(e);
        } catch (MailException e) {
            logMailSendingFailed(e);
            throwWhenCanRetry(e);
            handleAmbiguous(message);
        } catch (Exception e) {
            log.error("예기치 않은 오류 발생: {}", e.getMessage(), e);
            handleFailure(message);
        }
    }

    protected void logMailSendingFailed(Exception e) {
        log.error("메일 전송 실패 : {}", e.getMessage(), e);
    }

    private void throwWhenCanRetry(Exception e) {
        if (RetryableMailException.canSwitchRetryableException(e)) {
            throw new RetryableMailException(e);
        }
    }

    protected abstract void logSending(T message);

    protected abstract void handleSuccess(T message);

    protected abstract void handleFailure(T message);

    protected abstract void handleAmbiguous(T message);
}
