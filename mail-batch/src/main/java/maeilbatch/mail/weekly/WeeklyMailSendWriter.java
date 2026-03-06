package maeilbatch.mail.weekly;

import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilbatch.forward.ForwardRepository;
import maeilbatch.mail.AbstractMailPayload;
import maeilmail.question.Question;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import maeilmail.subscribe.command.domain.SubscribeQuestionRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyMailSendWriter implements ItemWriter<AbstractMailPayload> {

    private final SubscribeQuestionRepository subscribeQuestionRepository;
    private final ForwardRepository forwardRepository;

    @Override
    public void write(Chunk<? extends AbstractMailPayload> chunk) {
        List<? extends AbstractMailPayload> items = chunk.getItems();
        items.forEach(this::rollingHistory);
        items.forEach(this::saveSendLog);
    }

    private void rollingHistory(AbstractMailPayload payload) {
        WeeklyMailPayload weeklyMailPayload = (WeeklyMailPayload) payload;
        List<Question> questions = weeklyMailPayload.getQuestions();
        removeAlreadySaved(weeklyMailPayload.getSubscribe(), questions);

        List<SubscribeQuestion> subscribeQuestions = questions.stream()
                .map(it -> SubscribeQuestion.success(weeklyMailPayload.getSubscribe(), it))
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

    private void saveSendLog(AbstractMailPayload payload) {
        forwardRepository.save(payload.toForwardLog());
    }
}
