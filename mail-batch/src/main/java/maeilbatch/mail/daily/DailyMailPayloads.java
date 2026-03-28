package maeilbatch.mail.daily;

import java.util.List;
import maeilbatch.mail.AbstractMailPayload;
import maeilbatch.mail.AbstractMailPayloads;
import maeilbatch.mail.dao.SubscribeQuestionKey;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import org.springframework.batch.item.Chunk;

public class DailyMailPayloads extends AbstractMailPayloads<DailyMailPayload> {

    public DailyMailPayloads(List<DailyMailPayload> payloads) {
        super(payloads);
    }

    public static DailyMailPayloads withChunk(Chunk<? extends AbstractMailPayload> chunk) {
        validateChunkItemType(chunk);

        List<DailyMailPayload> payloads = chunk.getItems().stream()
                .map(DailyMailPayload.class::cast)
                .toList();

        return new DailyMailPayloads(payloads);
    }

    private static void validateChunkItemType(Chunk<? extends AbstractMailPayload> chunk) {
        for (AbstractMailPayload item : chunk.getItems()) {
            if (!(item instanceof DailyMailPayload)) {
                throw new IllegalArgumentException("잘못된 payload 타입입니다.");
            }
        }
    }

    public List<SubscribeQuestion> toSubscribeQuestions() {
        return payloads.stream()
                .map(it -> SubscribeQuestion.success(it.getSubscribe(), it.getQuestion()))
                .toList();
    }

    public List<SubscribeQuestionKey> getSubscribeQuestionKeys() {
        return payloads.stream()
                .map(it -> new SubscribeQuestionKey(it.getSubscribe().getId(), it.getQuestion().getId()))
                .distinct()
                .toList();
    }
}
