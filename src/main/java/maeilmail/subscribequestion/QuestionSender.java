package maeilmail.subscribequestion;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.Subscribe;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component(value = "questionSender")
@RequiredArgsConstructor
public class QuestionSender {

    private static final int MAIL_SENDER_RATE_MILLISECONDS = 500;
    private static final String FROM_EMAIL = "maeil-mail <maeil-mail-noreply@maeil-mail.site>";

    private final JavaMailSender javaMailSender;
    private final SubscribeQuestionRepository subscribeQuestionRepository;

    @Async
    public void sendMail(SubscribeQuestionMessage message) {
        Subscribe subscribe = message.subscribe();
        Question question = message.question();
        QuestionCategory questionCategory = question.getCategory();

        String to = subscribe.getEmail();
        String subject = "[매일메일] " + message.subject();
        String text = message.text();
        String category = questionCategory.toLowerCase();
        try {
            log.info("질문지를 전송합니다. email = {}, questionId = {}, subject = {}, category = {}", to, question.getId(), subject, category);
            MimeMessage mimeMessage = convertToMime(to, subject, text);
            javaMailSender.send(mimeMessage);
            subscribeQuestionRepository.save(SubscribeQuestion.success(subscribe, question));
        } catch (MessagingException | MailException e) {
            log.error("메일 전송 실패: email = {}, questionId = {}, subject = {}, category = {}, 오류 = {}", to, question.getId(), subject, category.toLowerCase(), e.getMessage(), e);
            subscribeQuestionRepository.save(SubscribeQuestion.fail(subscribe, question));
        } catch (Exception e) {
            log.error("예기치 않은 오류 발생: email = {}, questionId = {}, subject = {}, category = {}, 오류 = {}", to, question.getId(), subject, category.toLowerCase(), e.getMessage(), e);
            subscribeQuestionRepository.save(SubscribeQuestion.fail(subscribe, question));
        } finally {
            try {
                Thread.sleep(MAIL_SENDER_RATE_MILLISECONDS);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private MimeMessage convertToMime(String to, String subject, String text) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        tryAppendOpenEventTrace(mimeMessage);
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        mimeMessageHelper.setFrom(FROM_EMAIL);
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(text, true);
        return mimeMessage;
    }

    private void tryAppendOpenEventTrace(MimeMessage mimeMessage) throws MessagingException {
        mimeMessage.setHeader("X-SES-CONFIGURATION-SET", "my-first-configuration-set");
        mimeMessage.setHeader("X-SES-MESSAGE_TAGS", "mail-open");
    }
}
