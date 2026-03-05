package maeilbatch.mail.daily;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import maeilmail.mail.MailViewRenderer;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class DailyMailViewTest {

    @Test
    @DisplayName("질문/구독자 정보를 템플릿 속성으로 렌더러에 전달한다.")
    void render() {
        MailViewRenderer renderer = Mockito.mock(MailViewRenderer.class);
        Subscribe subscribe = new Subscribe("daily@test.com", QuestionCategory.BACKEND, SubscribeFrequency.DAILY);
        Question question = new Question(1L, "question-title", "question-content", QuestionCategory.BACKEND);
        DailyMailView view = DailyMailView.builder()
                .renderer(renderer)
                .subscribe(subscribe)
                .question(question)
                .build();
        ArgumentCaptor<Map<Object, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        when(renderer.render(anyMap(), eq("question-v4"))).thenReturn("rendered");

        String result = view.render();

        verify(renderer).render(mapCaptor.capture(), eq("question-v4"));
        Map<Object, Object> attributes = mapCaptor.getValue();
        assertThat(result).isEqualTo("rendered");
        assertThat(attributes.get("questionId")).isEqualTo(1L);
        assertThat(attributes.get("question")).isEqualTo("question-title");
        assertThat(attributes.get("email")).isEqualTo("daily@test.com");
        assertThat(attributes.get("token")).isEqualTo(subscribe.getToken());
        assertThat(view.getType()).isEqualTo("question");
    }
}
