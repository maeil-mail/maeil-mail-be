package maeilbatch.mail.weekly;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import maeilmail.mail.AbstractMailSender;
import maeilmail.mail.MimeMessageCustomizer;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import maeilmail.subscribe.command.domain.SubscribeQuestionRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component("weeklyMailSender")
public class WeeklyMailSender extends AbstractMailSender<WeeklyMailMessage> {

    private final SubscribeQuestionRepository subscribeQuestionRepository;

    public WeeklyMailSender(
            JavaMailSender javaMailSender,
            MimeMessageCustomizer mimeMessageCustomizer,
            SubscribeQuestionRepository subscribeQuestionRepository
    ) {
        super(javaMailSender, mimeMessageCustomizer);

        this.subscribeQuestionRepository = subscribeQuestionRepository;
    }

    @Override
    protected void logSending(WeeklyMailMessage message) {
        Subscribe subscribe = message.subscribe();
        List<Long> questions = message.questions().stream()
                .map(Question::getId)
                .toList();
        QuestionCategory category = subscribe.getCategory();

        log.info("주간 질문지를 전송합니다. email = {}, questionIds = {}, subject = {}, category = {}",
                subscribe.getEmail(), questions, message.subject(), category.toLowerCase());
    }

    @Override
    @Transactional
    protected void handleSuccess(WeeklyMailMessage message) {
        List<Question> questions = message.questions();
        removeAlreadySaved(message.subscribe(), questions);

        List<SubscribeQuestion> subscribeQuestions = questions.stream()
                .map(it -> SubscribeQuestion.success(message.subscribe(), it))
                .toList();

        subscribeQuestionRepository.saveAll(subscribeQuestions);
    }

    private void removeAlreadySaved(Subscribe subscribe, List<Question> questions) {
        List<SubscribeQuestion> alreadySaved = subscribeQuestionRepository.findBySubscribeAndQuestionIn(subscribe, questions);
        List<Long> removeTargetIds = alreadySaved.stream()
                .map(SubscribeQuestion::getId)
                .toList();

        subscribeQuestionRepository.removeAllByIdIn(removeTargetIds);
    }

    @Override
    protected void handleFailure(WeeklyMailMessage message) {
        List<Question> questions = message.questions();
        List<SubscribeQuestion> subscribeQuestions = questions.stream()
                .map(it -> SubscribeQuestion.fail(message.subscribe(), it))
                .toList();

        subscribeQuestionRepository.saveAll(subscribeQuestions);
    }
}
