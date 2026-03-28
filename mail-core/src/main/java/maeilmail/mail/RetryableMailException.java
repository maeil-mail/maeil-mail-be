package maeilmail.mail;

import java.net.SocketTimeoutException;
import org.springframework.mail.MailSendException;

class RetryableMailException extends RuntimeException {

    public RetryableMailException(Throwable cause) {
        super(cause);
    }

    public static boolean canSwitchRetryableException(Throwable throwable) {
        if (!(throwable instanceof MailSendException mse)) {
            return true;
        }

        int length = mse.getMessageExceptions().length;
        if (length == 0) {
            return !isSocketTimeout(mse.getCause());
        }

        for (Exception messageException : mse.getMessageExceptions()) {
            Throwable current = messageException;
            while (current != null) {
                if (isSocketTimeout(current)) {
                    return false;
                }

                current = current.getCause();
            }
        }

        return true;
    }

    private static boolean isSocketTimeout(Throwable current) {
        return current instanceof SocketTimeoutException;
    }
}
