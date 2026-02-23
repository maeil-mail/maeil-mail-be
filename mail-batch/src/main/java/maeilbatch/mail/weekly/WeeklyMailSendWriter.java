package maeilbatch.mail.weekly;

import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilbatch.forward.ForwardLog;
import maeilbatch.forward.ForwardRepository;
import maeilmail.mail.MailMessage;
import maeilmail.question.Question;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import maeilmail.subscribe.command.domain.SubscribeQuestionRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyMailSendWriter implements ItemWriter<MailMessage> {

    private final SubscribeQuestionRepository subscribeQuestionRepository;
    private final ForwardRepository forwardRepository;

    @Override
    public void write(Chunk<? extends MailMessage> chunk) {
        List<? extends MailMessage> items = chunk.getItems();
        items.forEach(this::rollingHistory);
        items.forEach(this::saveSendLog);
    }

    private void rollingHistory(MailMessage message) {
        WeeklyMailMessage weeklyMailMessage = (WeeklyMailMessage) message;
        List<Question> questions = weeklyMailMessage.questions();
        removeAlreadySaved(weeklyMailMessage.subscribe(), questions);

        List<SubscribeQuestion> subscribeQuestions = questions.stream()
                .map(it -> SubscribeQuestion.success(weeklyMailMessage.subscribe(), it))
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

    private void saveSendLog(MailMessage message) {
        ForwardLog forwardLog = new ForwardLog(message.getTo(), message.getSubject(), message.getText());
        forwardRepository.save(forwardLog);
    }
}
