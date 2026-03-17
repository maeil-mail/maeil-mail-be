package maeilbatch.mail.weekly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import maeilbatch.mail.AbstractMailPayload;
import maeilbatch.mail.daily.DailyMailPayload;
import maeilbatch.mail.dao.SubscribeQuestionKey;
import maeilbatch.forward.ForwardStatus;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;

class WeeklyMailPayloadsTest {

    @Test
    @DisplayName("청크에 주간 payload 외 타입이 포함되면 예외가 발생한다.")
    void withChunkThrowsWhenChunkContainsInvalidType() {
        Subscribe subscribe = createSubscribe(1L, "weekly@test.com", SubscribeFrequency.WEEKLY);
        DailyMailPayload invalidPayload = new DailyMailPayload(
                subscribe,
                createQuestion(1L, "title-1"),
                "subject",
                "text"
        );
        Chunk<? extends AbstractMailPayload> chunk = new Chunk<>(List.of(invalidPayload));

        assertThatThrownBy(() -> WeeklyMailPayloads.withChunk(chunk))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("잘못된 payload 타입입니다.");
    }

    @Test
    @DisplayName("주간 payload가 없으면 isEmpty는 true를 반환한다.")
    void isEmptyWhenNoPayload() {
        WeeklyMailPayloads payloads = new WeeklyMailPayloads(List.of());

        assertThat(payloads.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("주간 payload를 SubscribeQuestion 목록으로 변환한다.")
    void toSubscribeQuestions() {
        Subscribe subscribe1 = createSubscribe(1L, "weekly-1@test.com", SubscribeFrequency.WEEKLY);
        Subscribe subscribe2 = createSubscribe(2L, "weekly-2@test.com", SubscribeFrequency.WEEKLY);
        List<Question> questions1 = List.of(
                createQuestion(10L, "q1"),
                createQuestion(11L, "q2"),
                createQuestion(12L, "q3")
        );
        List<Question> questions2 = List.of(
                createQuestion(13L, "q4"),
                createQuestion(14L, "q5")
        );
        WeeklyMailPayload payload1 = createPayload(subscribe1, questions1, "weekly-subject-1", "weekly-text-1");
        WeeklyMailPayload payload2 = createPayload(subscribe2, questions2, "weekly-subject-2", "weekly-text-2");
        WeeklyMailPayloads payloads = new WeeklyMailPayloads(List.of(payload1, payload2));

        List<SubscribeQuestion> result = payloads.toSubscribeQuestions();

        assertAll(
                () -> assertThat(result).hasSize(5),
                () -> assertThat(result)
                        .extracting(it -> it.getQuestion().getId())
                        .containsExactlyInAnyOrder(10L, 11L, 12L, 13L, 14L),
                () -> assertThat(result)
                        .extracting(it -> it.getSubscribe().getId())
                        .containsExactlyInAnyOrder(1L, 1L, 1L, 2L, 2L),
                () -> assertThat(result)
                        .extracting(SubscribeQuestion::isSuccess)
                        .containsOnly(true)
        );
    }

    @Test
    @DisplayName("주간 payload를 forward 로그 목록으로 변환한다.")
    void toForwardLogs() {
        Subscribe subscribe = createSubscribe(1L, "weekly@test.com", SubscribeFrequency.WEEKLY);
        WeeklyMailPayload payload = createPayload(
                subscribe,
                List.of(createQuestion(10L, "q1"), createQuestion(11L, "q2")),
                "weekly-subject",
                "weekly-text"
        );
        WeeklyMailPayloads payloads = new WeeklyMailPayloads(List.of(payload));

        var result = payloads.toForwardLogs();

        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.get(0).getTarget()).isEqualTo("weekly@test.com"),
                () -> assertThat(result.get(0).getSubject()).isEqualTo("weekly-subject"),
                () -> assertThat(result.get(0).getMessage()).isEqualTo("weekly-text"),
                () -> assertThat(result.get(0).getStatus()).isEqualTo(ForwardStatus.PENDING)
        );
    }

    @Test
    @DisplayName("질문이 중복되면 SubscribeQuestionKey는 중복 없이 반환한다.")
    void getSubscribeQuestionKeysDistinct() {
        Subscribe subscribe = createSubscribe(1L, "weekly@test.com", SubscribeFrequency.WEEKLY);
        Question duplicated = createQuestion(10L, "duplicated");
        WeeklyMailPayload payload1 = createPayload(subscribe, List.of(duplicated, createQuestion(11L, "q2")), "s1", "t1");
        WeeklyMailPayload payload2 = createPayload(subscribe, List.of(duplicated), "s2", "t2");
        WeeklyMailPayloads payloads = new WeeklyMailPayloads(List.of(payload1, payload2));

        List<SubscribeQuestionKey> result = payloads.getSubscribeQuestionKeys();

        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).containsExactlyInAnyOrder(
                        new SubscribeQuestionKey(1L, 10L),
                        new SubscribeQuestionKey(1L, 11L)
                )
        );
    }

    @Test
    @DisplayName("중복이 없는 주간 payload는 SubscribeQuestionKey를 모두 반환한다.")
    void getSubscribeQuestionKeysAllWhenNoDuplicate() {
        Subscribe subscribe1 = createSubscribe(1L, "weekly-1@test.com", SubscribeFrequency.WEEKLY);
        Subscribe subscribe2 = createSubscribe(2L, "weekly-2@test.com", SubscribeFrequency.WEEKLY);
        WeeklyMailPayload payload1 = createPayload(
                subscribe1,
                List.of(createQuestion(10L, "q1"), createQuestion(11L, "q2")),
                "subject-1",
                "text-1"
        );
        WeeklyMailPayload payload2 = createPayload(
                subscribe2,
                List.of(createQuestion(12L, "q3")),
                "subject-2",
                "text-2"
        );
        WeeklyMailPayloads payloads = new WeeklyMailPayloads(List.of(payload1, payload2));

        List<SubscribeQuestionKey> result = payloads.getSubscribeQuestionKeys();

        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result).containsExactlyInAnyOrder(
                        new SubscribeQuestionKey(1L, 10L),
                        new SubscribeQuestionKey(1L, 11L),
                        new SubscribeQuestionKey(2L, 12L)
                )
        );
    }

    private WeeklyMailPayload createPayload(
            Subscribe subscribe,
            List<Question> questions,
            String subject,
            String text
    ) {
        return new WeeklyMailPayload(subscribe, questions, subject, text);
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
