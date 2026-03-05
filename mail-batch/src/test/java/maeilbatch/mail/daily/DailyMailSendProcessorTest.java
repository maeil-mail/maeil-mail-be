package maeilbatch.mail.daily;

import static org.assertj.core.api.Assertions.assertThat;
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

class DailyMailSendProcessorTest {

    @Test
    @DisplayName("질문 선택/뷰 렌더링에 성공하면 DailyMailMessage를 생성한다.")
    void processSuccess() {
        ChoiceQuestionPolicy policy = mock(ChoiceQuestionPolicy.class);
        MailViewRenderer renderer = mock(MailViewRenderer.class);
        DailyMailSendProcessor processor = new DailyMailSendProcessor(policy, renderer);
        Subscribe subscribe = new Subscribe("daily@test.com", QuestionCategory.BACKEND, SubscribeFrequency.DAILY);
        QuestionSummary questionSummary = new QuestionSummary(
                10L, "subject-title", "content", "backend", LocalDateTime.now(), LocalDateTime.now()
        );
        when(policy.choice(subscribe)).thenReturn(questionSummary);
        when(renderer.render(anyMap(), eq("question-v4"))).thenReturn("rendered-text");

        DailyMailMessage result = processor.process(subscribe);

        assertThat(result).isNotNull();
        assertThat(result.getTo()).isEqualTo("daily@test.com");
        assertThat(result.getSubject()).isEqualTo("subject-title");
        assertThat(result.getText()).isEqualTo("rendered-text");
        assertThat(result.question().getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("질문 선택 중 예외가 나면 null을 반환한다.")
    void processFail() {
        ChoiceQuestionPolicy policy = mock(ChoiceQuestionPolicy.class);
        MailViewRenderer renderer = mock(MailViewRenderer.class);
        DailyMailSendProcessor processor = new DailyMailSendProcessor(policy, renderer);
        Subscribe subscribe = new Subscribe("daily@test.com", QuestionCategory.BACKEND, SubscribeFrequency.DAILY);
        when(policy.choice(subscribe)).thenThrow(new IllegalStateException("fail"));

        DailyMailMessage result = processor.process(subscribe);

        assertThat(result).isNull();
    }
}
