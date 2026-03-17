package maeilbatch.mail.daily;

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
public class DailyMailSendWriter implements ItemWriter<AbstractMailPayload> {

    private final SubscribeQuestionDao subscribeQuestionDao;
    private final ForwardDao forwardDao;

    @Override
    public void write(Chunk<? extends AbstractMailPayload> chunk) {
        DailyMailPayloads dailyMailPayloads = DailyMailPayloads.withChunk(chunk);
        if (dailyMailPayloads.isEmpty()) {
            return;
        }

        rollingHistory(dailyMailPayloads);
        saveSendLogs(dailyMailPayloads);
    }

    private void rollingHistory(DailyMailPayloads payloads) {
        removeAlreadySaved(payloads);
        saveSubscribeQuestions(payloads);
    }

    private void removeAlreadySaved(DailyMailPayloads payloads) {
        List<SubscribeQuestionKey> keys = payloads.getSubscribeQuestionKeys();
        List<Long> removeTargetIds = subscribeQuestionDao.findIdsByKeys(keys);

        subscribeQuestionDao.batchDeleteByIds(removeTargetIds);
    }

    private void saveSubscribeQuestions(DailyMailPayloads payloads) {
        List<SubscribeQuestion> subscribeQuestions = payloads.toSubscribeQuestions();

        subscribeQuestionDao.batchInsert(subscribeQuestions);
    }

    private void saveSendLogs(DailyMailPayloads payloads) {
        List<ForwardLog> logs = payloads.toForwardLogs();

        forwardDao.batchInsert(logs);
    }
}
