package maeilbatch.mail.weekly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import maeilmail.mail.MailViewRenderer;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class WeeklyMailViewTest {

    private static final String SUBSCRIBE_EMAIL = "weekly@test.com";
    private static final LocalDate SEND_DATE = LocalDate.of(2025, 5, 5);

    @Test
    @DisplayName("질문/구독자 정보를 템플릿 속성으로 렌더러에 전달한다.")
    void render() {
        MailViewRenderer renderer = Mockito.mock(MailViewRenderer.class);
        Subscribe subscribe = createSubscribe();
        List<QuestionSummary> questionSummaries = createQuestionSummaries();
        WeeklyMailView view = WeeklyMailView.builder()
                .renderer(renderer)
                .date(SEND_DATE)
                .subscribe(subscribe)
                .questionSummaries(questionSummaries)
                .build();
        ArgumentCaptor<Map<Object, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        when(renderer.render(anyMap(), eq("weekly-question"))).thenReturn("rendered");

        String result = view.render();

        verify(renderer).render(mapCaptor.capture(), eq("weekly-question"));
        Map<Object, Object> attributes = mapCaptor.getValue();
        assertAll(
                () -> assertThat(result).isEqualTo("rendered"),
                () -> assertThat(attributes.get("questions")).isEqualTo(questionSummaries),
                () -> assertThat(attributes.get("category")).isEqualTo("backend"),
                () -> assertThat(attributes.get("email")).isEqualTo(SUBSCRIBE_EMAIL),
                () -> assertThat(attributes.get("token")).isEqualTo(subscribe.getToken()),
                () -> assertThat(attributes.get("weekLabel")).isEqualTo("BE 5월 1주차 질문"),
                () -> assertThat(attributes.get("year")).isEqualTo(2025),
                () -> assertThat(attributes.get("month")).isEqualTo(5),
                () -> assertThat(attributes.get("week")).isEqualTo(1),
                () -> assertThat(view.getType()).isEqualTo("question")
        );
    }

    private Subscribe createSubscribe() {
        return new Subscribe(SUBSCRIBE_EMAIL, QuestionCategory.BACKEND, SubscribeFrequency.WEEKLY);
    }

    private List<QuestionSummary> createQuestionSummaries() {
        return List.of(
                createQuestionSummary(1L),
                createQuestionSummary(2L)
        );
    }

    private QuestionSummary createQuestionSummary(Long id) {
        return new QuestionSummary(
                id,
                "question-title-" + id,
                "question-content-" + id,
                "backend",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
