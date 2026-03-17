package maeilbatch.mail.weekly;

import java.util.List;
import java.util.stream.Stream;
import maeilbatch.mail.AbstractMailPayload;
import maeilbatch.mail.AbstractMailPayloads;
import maeilbatch.mail.dao.SubscribeQuestionKey;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import org.springframework.batch.item.Chunk;

public class WeeklyMailPayloads extends AbstractMailPayloads<WeeklyMailPayload> {

    public WeeklyMailPayloads(List<WeeklyMailPayload> payloads) {
        super(payloads);
    }

    public static WeeklyMailPayloads withChunk(Chunk<? extends AbstractMailPayload> chunk) {
        validateChunkItemType(chunk);

        List<WeeklyMailPayload> payloads = chunk.getItems().stream()
                .map(WeeklyMailPayload.class::cast)
                .toList();

        return new WeeklyMailPayloads(payloads);
    }

    private static void validateChunkItemType(Chunk<? extends AbstractMailPayload> chunk) {
        for (AbstractMailPayload item : chunk.getItems()) {
            if (!(item instanceof WeeklyMailPayload)) {
                throw new IllegalArgumentException("잘못된 payload 타입입니다.");
            }
        }
    }

    public List<SubscribeQuestion> toSubscribeQuestions() {
        return payloads.stream()
                .flatMap(this::getSubscribeQuestionStream)
                .toList();
    }

    private Stream<SubscribeQuestion> getSubscribeQuestionStream(WeeklyMailPayload payload) {
        return payload.getQuestions().stream()
                .map(question -> SubscribeQuestion.success(payload.getSubscribe(), question));
    }

    public List<SubscribeQuestionKey> getSubscribeQuestionKeys() {
        return payloads.stream()
                .flatMap(this::getSubscribeQuestionKeyStream)
                .distinct()
                .toList();
    }

    private Stream<SubscribeQuestionKey> getSubscribeQuestionKeyStream(WeeklyMailPayload payload) {
        return payload.getQuestions().stream()
                .map(question -> new SubscribeQuestionKey(payload.getSubscribe().getId(), question.getId()));
    }
}
