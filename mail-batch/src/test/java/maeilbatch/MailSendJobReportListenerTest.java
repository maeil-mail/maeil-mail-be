package maeilbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import maeilmail.mail.MailSender;
import maeilmail.mail.MailViewRenderer;
import maeilmail.mail.SimpleMailMessage;
import maeilmail.statistics.DailySendReport;
import maeilmail.statistics.StatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.core.JobExecution;
import org.springframework.test.util.ReflectionTestUtils;

class MailSendJobReportListenerTest {

    @Test
    @DisplayName("잡 종료 후 일간 전송 리포트를 렌더링해 관리자 메일로 전송한다.")
    void afterJob() {
        MailSender mailSender = mock(MailSender.class);
        StatisticsService statisticsService = mock(StatisticsService.class);
        MailViewRenderer mailViewRenderer = mock(MailViewRenderer.class);
        MailSendJobReportListener listener = new MailSendJobReportListener(
                mailSender,
                statisticsService,
                mailViewRenderer
        );
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 5, 1, 7, 0);
        ReflectionTestUtils.setField(listener, "dateTime", baseDateTime);
        DailySendReport report = new DailySendReport(100L, 95L, 90L, 5L);
        when(statisticsService.generateDailySendReport(baseDateTime.toLocalDate())).thenReturn(report);
        when(mailViewRenderer.render(org.mockito.ArgumentMatchers.anyMap(), org.mockito.ArgumentMatchers.eq("report")))
                .thenReturn("rendered-report");
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        listener.afterJob(new JobExecution(1L));

        verify(statisticsService, times(1)).generateDailySendReport(LocalDate.of(2025, 5, 1));
        verify(mailSender, times(1)).sendMail(messageCaptor.capture());
        SimpleMailMessage result = messageCaptor.getValue();

        assertAll(
                () -> assertThat(result.to()).isEqualTo("team.maeilmail@gmail.com"),
                () -> assertThat(result.subject()).isEqualTo("관리자 전용 메일 전송 결과를 알려드립니다."),
                () -> assertThat(result.text()).isEqualTo("rendered-report"),
                () -> assertThat(result.type()).isEqualTo("report")
        );
    }
}
