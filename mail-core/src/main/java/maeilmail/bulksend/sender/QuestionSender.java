package maeilmail.bulksend.sender;

import lombok.extern.slf4j.Slf4j;
import maeilmail.mail.AbstractMailSender;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import maeilmail.subscribe.command.domain.SubscribeQuestionRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component("questionSender")
public class QuestionSender extends AbstractMailSender<SubscribeQuestionMessage> {

    private final SubscribeQuestionRepository subscribeQuestionRepository;

    public QuestionSender(
            JavaMailSender javaMailSender,
            QuestionMimeMessageCustomizer mimeMessageCustomizer,
            SubscribeQuestionRepository subscribeQuestionRepository
    ) {
        super(javaMailSender, mimeMessageCustomizer);
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
    @Transactional
    protected void handleSuccess(SubscribeQuestionMessage message) {
        subscribeQuestionRepository
                .findBySubscribeAndQuestion(message.subscribe(), message.question())
                .ifPresent(subscribeQuestionRepository::delete);

        subscribeQuestionRepository.save(SubscribeQuestion.success(message.subscribe(), message.question()));
    }

    @Override
    protected void handleFailure(SubscribeQuestionMessage message) {
        subscribeQuestionRepository.save(SubscribeQuestion.fail(message.subscribe(), message.question()));
    }
}
