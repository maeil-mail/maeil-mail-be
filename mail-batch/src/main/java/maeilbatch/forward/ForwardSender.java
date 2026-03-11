package maeilbatch.forward;

import lombok.extern.slf4j.Slf4j;
import maeilmail.RateLimiter;
import maeilmail.mail.AbstractMailSender;
import maeilmail.mail.MimeMessageCustomizer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component("forwardMailSender")
public class ForwardSender extends AbstractMailSender<ForwardLog> {

    private final ForwardDao forwardDao;

    public ForwardSender(
            JavaMailSender javaMailSender,
            MimeMessageCustomizer mimeMessageCustomizer,
            RateLimiter rateLimiter,
            ForwardDao forwardDao
    ) {
        super(javaMailSender, mimeMessageCustomizer, rateLimiter);
        this.forwardDao = forwardDao;
    }

    @Override
    protected void logSending(ForwardLog forwardLog) {
        log.info("질문지를 전송합니다. email = {} subject = {}", forwardLog.getTarget(), forwardLog.getSubject());
    }

    @Override
    protected void handleSuccess(ForwardLog forwardLog) {
        forwardLog.setStatus(ForwardStatus.DONE);
        forwardDao.changeState(forwardLog.getId(), ForwardStatus.DONE);
    }

    @Override
    protected void handleFailure(ForwardLog forwardLog) {
        forwardLog.setStatus(ForwardStatus.FAILED);
        forwardDao.changeState(forwardLog.getId(), ForwardStatus.FAILED);
    }
}
