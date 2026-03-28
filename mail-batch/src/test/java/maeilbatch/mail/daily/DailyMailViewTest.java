package maeilbatch.mail.daily;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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

class DailyMailViewTest {

    private static final Long QUESTION_ID = 1L;
    private static final String QUESTION_TITLE = "question-title";
    private static final String SUBSCRIBE_EMAIL = "daily@test.com";

    @Test
    @DisplayName("질문/구독자 정보를 템플릿 속성으로 렌더러에 전달한다.")
    void render() {
        MailViewRenderer renderer = mock(MailViewRenderer.class);
        Subscribe subscribe = createSubscribe();
        Question question = createQuestion();
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
        assertAll(
                () -> assertThat(result).isEqualTo("rendered"),
                () -> assertThat(attributes.get("questionId")).isEqualTo(QUESTION_ID),
                () -> assertThat(attributes.get("question")).isEqualTo(QUESTION_TITLE),
                () -> assertThat(attributes.get("email")).isEqualTo(SUBSCRIBE_EMAIL),
                () -> assertThat(attributes.get("token")).isEqualTo(subscribe.getToken()),
                () -> assertThat(view.getType()).isEqualTo("question")
        );
    }

    private Subscribe createSubscribe() {
        return new Subscribe(SUBSCRIBE_EMAIL, QuestionCategory.BACKEND, SubscribeFrequency.DAILY);
    }

    private Question createQuestion() {
        return new Question(QUESTION_ID, QUESTION_TITLE, "question-content", QuestionCategory.BACKEND);
    }
}
