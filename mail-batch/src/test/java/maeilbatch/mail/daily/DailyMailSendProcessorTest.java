package maeilbatch.mail.daily;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
import org.mockito.Mockito;

class DailyMailSendProcessorTest {

    private static final String SUBSCRIBE_EMAIL = "daily@test.com";
    private static final Long QUESTION_ID = 10L;
    private static final String QUESTION_TITLE = "subject-title";

    @Test
    @DisplayName("질문 선택/뷰 렌더링에 성공하면 DailyMailMessage를 생성한다.")
    void processSuccess() {
        ChoiceQuestionPolicy policy = Mockito.mock(ChoiceQuestionPolicy.class);
        MailViewRenderer renderer = mock(MailViewRenderer.class);
        DailyMailSendProcessor processor = createProcessor(policy, renderer);
        Subscribe subscribe = createSubscribe();
        QuestionSummary questionSummary = createQuestionSummary();
        when(policy.choice(subscribe)).thenReturn(questionSummary);
        when(renderer.render(anyMap(), eq("question-v4"))).thenReturn("rendered-text");

        DailyMailMessage result = processor.process(subscribe);

        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getTo()).isEqualTo(SUBSCRIBE_EMAIL),
                () -> assertThat(result.getSubject()).isEqualTo(QUESTION_TITLE),
                () -> assertThat(result.getText()).isEqualTo("rendered-text"),
                () -> assertThat(result.question().getId()).isEqualTo(QUESTION_ID)
        );
    }

    @Test
    @DisplayName("질문 선택 중 예외가 나면 null을 반환한다.")
    void processFail() {
        ChoiceQuestionPolicy policy = Mockito.mock(ChoiceQuestionPolicy.class);
        DailyMailSendProcessor processor = createProcessor(policy, mock(MailViewRenderer.class));
        Subscribe subscribe = createSubscribe();
        when(policy.choice(subscribe)).thenThrow(new IllegalStateException("fail"));

        DailyMailMessage result = processor.process(subscribe);

        assertThat(result).isNull();
    }

    private DailyMailSendProcessor createProcessor(ChoiceQuestionPolicy policy, MailViewRenderer renderer) {
        return new DailyMailSendProcessor(policy, renderer);
    }

    private Subscribe createSubscribe() {
        return new Subscribe(SUBSCRIBE_EMAIL, QuestionCategory.BACKEND, SubscribeFrequency.DAILY);
    }

    private QuestionSummary createQuestionSummary() {
        return new QuestionSummary(
                QUESTION_ID,
                QUESTION_TITLE,
                "content",
                "backend",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
