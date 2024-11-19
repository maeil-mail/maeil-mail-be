package maeilmail.subscribequestion;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import maeilmail.AbstractMailSender;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.Subscribe;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component("questionSender")
public class QuestionSender extends AbstractMailSender<SubscribeQuestionMessage> {

    private final SubscribeQuestionRepository subscribeQuestionRepository;

    public QuestionSender(JavaMailSender javaMailSender, SubscribeQuestionRepository subscribeQuestionRepository) {
        super(javaMailSender);
        this.subscribeQuestionRepository = subscribeQuestionRepository;
    }

    @Override
    protected void logSending(SubscribeQuestionMessage message) {
        Subscribe subscribe = message.subscribe();
        Question question = message.question();
        QuestionCategory category = question.getCategory();
        log.info("질문지를 전송합니다. email = {}, questionId = {}, subject = {}, category = {}",
                subscribe.getEmail(), question.getId(), message.subject(), category.toLowerCase());
    }

    @Override
    protected MimeMessage createMimeMessage(SubscribeQuestionMessage message) throws MessagingException {
        Subscribe subscribe = message.subscribe();

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        mimeMessage.setHeader("X-SES-CONFIGURATION-SET", "my-first-configuration-set");
        mimeMessage.setHeader("X-SES-MESSAGE-TAGS", "mail-open");

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setFrom(FROM_EMAIL);
        helper.setTo(subscribe.getEmail());
        helper.setSubject("[매일메일] " + message.subject());
        helper.setText(message.text(), true);
        return mimeMessage;
    }

    @Override
    protected void handleSuccess(SubscribeQuestionMessage message) {
        subscribeQuestionRepository.save(SubscribeQuestion.success(message.subscribe(), message.question()));
    }

    @Override
    protected void handleFailure(SubscribeQuestionMessage message) {
        subscribeQuestionRepository.save(SubscribeQuestion.fail(message.subscribe(), message.question()));
    }
}
