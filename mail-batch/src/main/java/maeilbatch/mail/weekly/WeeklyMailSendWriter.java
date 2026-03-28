package maeilbatch.mail.weekly;

import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilbatch.forward.ForwardDao;
import maeilbatch.forward.ForwardLog;
import maeilbatch.mail.AbstractMailPayload;
import maeilbatch.mail.dao.SubscribeQuestionDao;
import maeilbatch.mail.dao.SubscribeQuestionKey;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyMailSendWriter implements ItemWriter<AbstractMailPayload> {

    private final SubscribeQuestionDao subscribeQuestionDao;
    private final ForwardDao forwardDao;

    @Override
    public void write(Chunk<? extends AbstractMailPayload> chunk) {
        WeeklyMailPayloads weeklyMailPayloads = WeeklyMailPayloads.withChunk(chunk);
        if (weeklyMailPayloads.isEmpty()) {
            return;
        }

        rollingHistory(weeklyMailPayloads);
        saveSendLogs(weeklyMailPayloads);
    }

    private void rollingHistory(WeeklyMailPayloads payloads) {
        removeAlreadySaved(payloads);
        saveSubscribeQuestions(payloads);
    }

    private void removeAlreadySaved(WeeklyMailPayloads payloads) {
        List<SubscribeQuestionKey> keys = payloads.getSubscribeQuestionKeys();
        List<Long> removeTargetIds = subscribeQuestionDao.findIdsByKeys(keys);

        subscribeQuestionDao.deleteByIds(removeTargetIds);
    }

    private void saveSubscribeQuestions(WeeklyMailPayloads payloads) {
        List<SubscribeQuestion> subscribeQuestions = payloads.toSubscribeQuestions();

        subscribeQuestionDao.batchInsert(subscribeQuestions);
    }

    private void saveSendLogs(WeeklyMailPayloads payloads) {
        List<ForwardLog> logs = payloads.toForwardLogs();

        forwardDao.batchInsert(logs);
    }
}
