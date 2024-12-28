package maeilmail.subscribequestion;

import lombok.extern.slf4j.Slf4j;
import maeilmail.mail.AbstractMailSender;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.command.domain.Subscribe;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component("questionSender")
public class QuestionSender extends AbstractMailSender<SubscribeQuestionMessage, QuestionMimeMessageCreator> {

    private final SubscribeQuestionRepository subscribeQuestionRepository;

    public QuestionSender(JavaMailSender javaMailSender, QuestionMimeMessageCreator mimeMessageCreator, SubscribeQuestionRepository subscribeQuestionRepository) {
        super(javaMailSender, mimeMessageCreator);
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
    protected void handleSuccess(SubscribeQuestionMessage message) {
        subscribeQuestionRepository.save(SubscribeQuestion.success(message.subscribe(), message.question()));
    }

    @Override
    protected void handleFailure(SubscribeQuestionMessage message) {
        subscribeQuestionRepository.save(SubscribeQuestion.fail(message.subscribe(), message.question()));
    }
}
