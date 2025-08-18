package maeilbatch.mail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.mail.MailSender;
import maeilmail.mail.MailView;
import maeilmail.mail.SimpleMailMessage;
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

    private static final String REPORT_TARGET = "team.maeilmail@gamil.com";
    private static final String REPORT_SUBJECT = "관리자 전용 메일 전송 결과를 알려드립니다.";

    private final MailSender mailSender;
    private final StatisticsService statisticsService;

    @Value("#{jobParameters['datetime']}")
    private LocalDateTime dateTime;

    @Override
    public void afterJob(JobExecution jobExecution) {
        LocalDate targetDate = dateTime.toLocalDate();
        DailySendReport dailySendReport = statisticsService.generateDailySendReport(targetDate);
        SimpleMailMessage message = createMessage(dailySendReport);

        mailSender.sendMail(message);
    }

    private SimpleMailMessage createMessage(DailySendReport report) {
        MailView view = createView(report);
        String text = view.render();

        return new SimpleMailMessage(REPORT_TARGET, REPORT_SUBJECT, text, view.getType());
    }

    private MailSendJobReportView createView(DailySendReport report) {
        return MailSendJobReportView.builder()
                .dailySendReport(report)
                .build();
    }
}
