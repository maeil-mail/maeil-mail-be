package maeilmail.subscribequestion;

import java.util.List;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import maeilmail.mail.AbstractMailSender;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.Subscribe;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component("weeklyQuestionSender")
public class WeeklyQuestionSender extends AbstractMailSender<WeeklySubscribeQuestionMessage> {

    private final SubscribeQuestionRepository subscribeQuestionRepository;

    public WeeklyQuestionSender(JavaMailSender javaMailSender, SubscribeQuestionRepository subscribeQuestionRepository) {
        super(javaMailSender);
        this.subscribeQuestionRepository = subscribeQuestionRepository;
    }

    @Override
    protected void logSending(WeeklySubscribeQuestionMessage message) {
        Subscribe subscribe = message.subscribe();
        List<Long> questions = message.questions().stream()
                .map(Question::getId)
                .toList();
        QuestionCategory category = subscribe.getCategory();

        log.info("주간 질문지를 전송합니다. email = {}, questionIds = {}, subject = {}, category = {}",
                subscribe.getEmail(), questions, message.subject(), category.toLowerCase());
    }

    @Override
    protected MimeMessage createMimeMessage(WeeklySubscribeQuestionMessage message) throws MessagingException {
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
    protected void handleSuccess(WeeklySubscribeQuestionMessage message) {
        List<Question> questions = message.questions();
        List<SubscribeQuestion> subscribeQuestions = questions.stream()
                .map(it -> SubscribeQuestion.success(message.subscribe(), it))
                .toList();

        subscribeQuestionRepository.saveAll(subscribeQuestions);
    }

    @Override
    protected void handleFailure(WeeklySubscribeQuestionMessage message) {
        List<Question> questions = message.questions();
        List<SubscribeQuestion> subscribeQuestions = questions.stream()
                .map(it -> SubscribeQuestion.fail(message.subscribe(), it))
                .toList();

        subscribeQuestionRepository.saveAll(subscribeQuestions);
    }
}
