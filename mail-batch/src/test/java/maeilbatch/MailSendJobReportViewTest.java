package maeilbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import maeilmail.mail.MailViewRenderer;
import maeilmail.statistics.DailySendReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MailSendJobReportViewTest {

    @Test
    @DisplayName("전송 리포트 텍스트를 렌더러 템플릿 변수로 전달한다.")
    void render() {
        MailViewRenderer renderer = mock(MailViewRenderer.class);
        DailySendReport report = new DailySendReport(100L, 95L, 90L, 5L);
        MailSendJobReportView view = MailSendJobReportView.builder()
                .renderer(renderer)
                .dailySendReport(report)
                .build();
        ArgumentCaptor<Map<Object, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        when(renderer.render(anyMap(), eq("report"))).thenReturn("rendered");

        String result = view.render();

        verify(renderer).render(mapCaptor.capture(), eq("report"));
        Map<Object, Object> attributes = mapCaptor.getValue();

        assertAll(
                () -> assertThat(result).isEqualTo("rendered"),
                () -> assertThat(attributes.get("report"))
                        .isEqualTo("질문 전송 카운트(전송 대상 건수/실제 전송 대상 건수/성공/실패) : 100/95/90/5"),
                () -> assertThat(view.getType()).isEqualTo("report")
        );
    }
}
