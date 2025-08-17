package maeilbatch.mail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.admin.view.AdminReportView;
import maeilmail.mail.MailMessage;
import maeilmail.mail.MailSender;
import maeilmail.statistics.DailySendReport;
import maeilmail.statistics.StatisticsService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@JobScope
@Component
@RequiredArgsConstructor
public class MailSendJobReportListener implements JobExecutionListener {

    private static final String REPORT_FORMAT = "질문 전송 카운트(전송 대상 건수/실제 전송 대상 건수/성공/실패) : %d/%d/%d/%d";
    private static final String REPORT_TARGET = "team.maeilmail@gamil.com";
    private static final String REPORT_SUBJECT = "관리자 전용 메일 전송 결과를 알려드립니다.";

    private final MailSender mailSender;
    private final AdminReportView adminReportView;
    private final StatisticsService statisticsService;

    @Value("#{jobParameters['datetime']}")
    private LocalDateTime baseDateTime;

    @Override
    public void afterJob(JobExecution jobExecution) {
        LocalDate targetDate = baseDateTime.toLocalDate();
        DailySendReport dailySendReport = statisticsService.generateDailySendReport(targetDate);
        MailMessage message = createMessage(dailySendReport);

        mailSender.sendMail(message);
    }

    private MailMessage createMessage(DailySendReport report) {
        String text = createText(report);

        return new MailMessage(REPORT_TARGET, REPORT_SUBJECT, text, adminReportView.getType());
    }

    private String createText(DailySendReport report) {
        String reportText = String.format(
                REPORT_FORMAT,
                report.expectedSendingCount(),
                report.actualSendingCount(),
                report.success(),
                report.fail()
        );

        return adminReportView.render(Map.of("report", reportText));
    }
}
