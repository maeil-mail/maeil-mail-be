package maeilbatch.mail.weekly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import maeilbatch.mail.ChoiceQuestionPolicy;
import maeilmail.mail.MailViewRenderer;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class WeeklyMailSendProcessorTest {

    private static final String SUBSCRIBE_EMAIL = "weekly@test.com";
    private static final String WEEKLY_SUBJECT = "이번주 면접 질문을 보내드려요.";
    private static final int WEEKLY_SEND_COUNT = SubscribeFrequency.WEEKLY.getSendCount();

    @Test
    @DisplayName("월요일에는 주간 질문지를 선택/렌더링하여 WeeklyMailPayload를 생성한다.")
    void processSuccess() {
        ChoiceQuestionPolicy policy = mock(ChoiceQuestionPolicy.class);
        MailViewRenderer renderer = mock(MailViewRenderer.class);
        WeeklyMailSendProcessor processor = createProcessor(policy, renderer, LocalDateTime.of(2025, 5, 5, 7, 0));
        Subscribe subscribe = createSubscribe();
        when(policy.choiceByRound(eq(subscribe), org.mockito.ArgumentMatchers.anyInt()))
                .thenAnswer(invocation -> createQuestionSummary(invocation.getArgument(1)));
        when(renderer.render(anyMap(), eq("weekly-question"))).thenReturn("rendered-text");

        WeeklyMailPayload result = processor.process(subscribe);

        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getSubscribe().getEmail()).isEqualTo(SUBSCRIBE_EMAIL),
                () -> assertThat(result.getSubject()).isEqualTo(WEEKLY_SUBJECT),
                () -> assertThat(result.getText()).isEqualTo("rendered-text"),
                () -> assertThat(result.getQuestions()).hasSize(WEEKLY_SEND_COUNT)
        );
        verify(policy, times(WEEKLY_SEND_COUNT)).choiceByRound(eq(subscribe), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    @DisplayName("월요일이 아니면 전송 메시지를 만들지 않는다.")
    void processSkipOnNotMonday() {
        ChoiceQuestionPolicy policy = mock(ChoiceQuestionPolicy.class);
        MailViewRenderer renderer = mock(MailViewRenderer.class);
        WeeklyMailSendProcessor processor = createProcessor(policy, renderer, LocalDateTime.of(2025, 5, 6, 7, 0));
        Subscribe subscribe = createSubscribe();

        WeeklyMailPayload result = processor.process(subscribe);

        assertThat(result).isNull();
        verifyNoInteractions(policy, renderer);
    }

    @Test
    @DisplayName("질문 선택 중 예외가 발생하면 null을 반환한다.")
    void processFail() {
        ChoiceQuestionPolicy policy = mock(ChoiceQuestionPolicy.class);
        MailViewRenderer renderer = mock(MailViewRenderer.class);
        WeeklyMailSendProcessor processor = createProcessor(policy, renderer, LocalDateTime.of(2025, 5, 5, 7, 0));
        Subscribe subscribe = createSubscribe();
        when(policy.choiceByRound(eq(subscribe), org.mockito.ArgumentMatchers.anyInt()))
                .thenThrow(new IllegalStateException("fail"));

        WeeklyMailPayload result = processor.process(subscribe);

        assertThat(result).isNull();
    }

    private WeeklyMailSendProcessor createProcessor(
            ChoiceQuestionPolicy policy,
            MailViewRenderer renderer,
            LocalDateTime dateTime
    ) {
        WeeklyMailSendProcessor processor = new WeeklyMailSendProcessor(policy, renderer);
        ReflectionTestUtils.setField(processor, "dateTime", dateTime);

        return processor;
    }

    private Subscribe createSubscribe() {
        return new Subscribe(SUBSCRIBE_EMAIL, QuestionCategory.BACKEND, SubscribeFrequency.WEEKLY);
    }

    private QuestionSummary createQuestionSummary(int round) {
        long id = round + 1L;
        return new QuestionSummary(
                id,
                "subject-title-" + id,
                "content-" + id,
                "backend",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
