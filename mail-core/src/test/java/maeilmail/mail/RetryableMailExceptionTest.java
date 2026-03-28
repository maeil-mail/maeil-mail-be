package maeilmail.mail;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.SocketTimeoutException;
import java.util.Map;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;

class RetryableMailExceptionTest {

    @Test
    @DisplayName("cause로 전달된 SocketTimeoutException은 retryable로 전환하지 않는다.")
    void returnsFalseWhenMailSendExceptionCauseIsSocketTimeout() {
        SocketTimeoutException socketTimeoutException = new SocketTimeoutException("Read timed out");
        MailSendException mailSendException = new MailSendException("메일 전송을 실패했습니다.", socketTimeoutException);

        boolean result = RetryableMailException.canSwitchRetryableException(mailSendException);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("messageExceptions에 담긴 SocketTimeoutException도 retryable로 전환하지 않는다.")
    void returnsFalseWhenMessageExceptionsContainSocketTimeout() {
        SocketTimeoutException socketTimeoutException = new SocketTimeoutException("Read timed out");
        MessagingException messagingException = new MessagingException("Exception reading response", socketTimeoutException);
        MailSendException mailSendException = new MailSendException(Map.of("message", messagingException));

        boolean result = RetryableMailException.canSwitchRetryableException(mailSendException);

        assertThat(mailSendException.getCause()).isNull();
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("cause로 전달된 일반 예외는 retryable로 전환한다.")
    void returnsTrueWhenMailSendExceptionCauseIsNotSocketTimeout() {
        IllegalStateException cause = new IllegalStateException("temporary smtp failure");
        MailSendException mailSendException = new MailSendException("메일 전송을 실패했습니다.", cause);

        boolean result = RetryableMailException.canSwitchRetryableException(mailSendException);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("messageExceptions에 SocketTimeoutException이 없으면 retryable로 전환한다.")
    void returnsTrueWhenMessageExceptionsDoNotContainSocketTimeout() {
        MessagingException messagingException =
                new MessagingException("Temporary local problem", new IllegalStateException("smtp 451"));
        MailSendException mailSendException = new MailSendException(Map.of("message", messagingException));

        boolean result = RetryableMailException.canSwitchRetryableException(mailSendException);

        assertThat(result).isTrue();
    }
}
