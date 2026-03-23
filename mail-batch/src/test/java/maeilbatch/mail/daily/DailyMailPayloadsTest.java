package maeilbatch.mail.daily;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import maeilbatch.forward.ForwardStatus;
import maeilbatch.mail.AbstractMailPayload;
import maeilbatch.mail.dao.SubscribeQuestionKey;
import maeilbatch.mail.weekly.WeeklyMailPayload;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;

class DailyMailPayloadsTest {

    @Test
    @DisplayName("청크에 일간 payload 외 타입이 포함되면 예외가 발생한다.")
    void withChunkThrowsWhenChunkContainsInvalidType() {
        Subscribe subscribe = createSubscribe(1L, "daily@test.com", SubscribeFrequency.DAILY);
        Question question = createQuestion(1L, "title-1");
        WeeklyMailPayload invalidPayload = new WeeklyMailPayload(subscribe, List.of(question), "subject", "text");
        Chunk<? extends AbstractMailPayload> chunk = new Chunk<>(List.of(invalidPayload));

        assertThatThrownBy(() -> DailyMailPayloads.withChunk(chunk))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("잘못된 payload 타입입니다.");
    }

    @Test
    @DisplayName("일간 payload가 없으면 isEmpty는 true를 반환한다.")
    void isEmptyWhenNoPayload() {
        DailyMailPayloads payloads = new DailyMailPayloads(List.of());

        assertThat(payloads.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("일간 payload를 SubscribeQuestion 목록으로 변환한다.")
    void toSubscribeQuestions() {
        Subscribe subscribe = createSubscribe(1L, "daily@test.com", SubscribeFrequency.DAILY);
        DailyMailPayload payload1 = createPayload(subscribe, createQuestion(10L, "question-title-1"), "mail-subject-1", "mail-text-1");
        DailyMailPayload payload2 = createPayload(subscribe, createQuestion(11L, "question-title-2"), "mail-subject-2", "mail-text-2");
        DailyMailPayloads payloads = new DailyMailPayloads(List.of(payload1, payload2));

        List<SubscribeQuestion> result = payloads.toSubscribeQuestions();

        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result)
                        .extracting(it -> it.getSubscribe().getId())
                        .containsOnly(1L),
                () -> assertThat(result)
                        .extracting(it -> it.getQuestion().getId())
                        .containsExactlyInAnyOrder(10L, 11L),
                () -> assertThat(result)
                        .extracting(SubscribeQuestion::isSuccess)
                        .containsOnly(true)
        );
    }

    @Test
    @DisplayName("일간 payload를 forward 로그 목록으로 변환한다.")
    void toForwardLogs() {
        Subscribe subscribe = createSubscribe(1L, "daily@test.com", SubscribeFrequency.DAILY);
        DailyMailPayload payload = createPayload(subscribe, createQuestion(10L, "question-title"), "mail-subject", "mail-text");
        DailyMailPayloads payloads = new DailyMailPayloads(List.of(payload));

        var result = payloads.toForwardLogs();

        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.get(0).getTarget()).isEqualTo("daily@test.com"),
                () -> assertThat(result.get(0).getSubject()).isEqualTo("mail-subject"),
                () -> assertThat(result.get(0).getMessage()).isEqualTo("mail-text"),
                () -> assertThat(result.get(0).getStatus()).isEqualTo(ForwardStatus.PENDING)
        );
    }

    @Test
    @DisplayName("중복된 일간 payload가 있어도 SubscribeQuestionKey는 중복 없이 반환한다.")
    void getSubscribeQuestionKeysDistinct() {
        Subscribe subscribe = createSubscribe(1L, "daily@test.com", SubscribeFrequency.DAILY);
        Question question = createQuestion(10L, "question-title");
        DailyMailPayload payload1 = createPayload(subscribe, question, "subject-1", "text-1");
        DailyMailPayload payload2 = createPayload(subscribe, question, "subject-2", "text-2");
        DailyMailPayloads payloads = new DailyMailPayloads(List.of(payload1, payload2));

        List<SubscribeQuestionKey> result = payloads.getSubscribeQuestionKeys();

        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.get(0)).isEqualTo(new SubscribeQuestionKey(1L, 10L))
        );
    }

    @Test
    @DisplayName("중복이 없는 일간 payload는 SubscribeQuestionKey를 모두 반환한다.")
    void getSubscribeQuestionKeysAllWhenNoDuplicate() {
        Subscribe subscribe1 = createSubscribe(1L, "daily-1@test.com", SubscribeFrequency.DAILY);
        Subscribe subscribe2 = createSubscribe(2L, "daily-2@test.com", SubscribeFrequency.DAILY);
        DailyMailPayload payload1 = createPayload(subscribe1, createQuestion(10L, "question-10"), "subject-1", "text-1");
        DailyMailPayload payload2 = createPayload(subscribe2, createQuestion(11L, "question-11"), "subject-2", "text-2");
        DailyMailPayloads payloads = new DailyMailPayloads(List.of(payload1, payload2));

        List<SubscribeQuestionKey> result = payloads.getSubscribeQuestionKeys();

        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).containsExactlyInAnyOrder(
                        new SubscribeQuestionKey(1L, 10L),
                        new SubscribeQuestionKey(2L, 11L)
                )
        );
    }

    private DailyMailPayload createPayload(Subscribe subscribe, Question question, String subject, String text) {
        return new DailyMailPayload(subscribe, question, subject, text);
    }

    private Subscribe createSubscribe(Long id, String email, SubscribeFrequency frequency) {
        return new Subscribe(
                id,
                email,
                QuestionCategory.BACKEND,
                0L,
                "token-%s".formatted(id),
                null,
                frequency
        );
    }

    private Question createQuestion(Long id, String title) {
        return new Question(
                id,
                title,
                "content-%s".formatted(id),
                QuestionCategory.BACKEND
        );
    }
}
