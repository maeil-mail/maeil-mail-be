package maeilmail.admin;

import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.mail.MailMessage;
import maeilmail.mail.MailSender;
import maeilmail.statistics.EventReport;
import maeilmail.statistics.StatisticsService;
import maeilmail.support.DistributedSupport;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class AdminReportScheduler {

    private static final String REPORT_FORMAT = "질문 전송 카운트(성공/실패) : %d/%d";

    private final MailSender mailSender;
    private final AdminRepository adminRepository;
    private final AdminReportView adminReportView;
    private final DistributedSupport distributedSupport;
    private final StatisticsService statisticsService;

    @Scheduled(cron = "0 30 7 * * MON-FRI", zone = "Asia/Seoul")
    public void sendReport() {
        log.info("관리자 결과 전송, date = {}", LocalDate.now());
        EventReport report = statisticsService.generateDailySubscribeQuestionReport();
        String text = createText(report);
        String subject = "관리자 전용 메일 전송 결과를 알려드립니다.";

        adminRepository.findAll().stream()
                .filter(it -> distributedSupport.isMine(it.getId()))
                .map(it -> createMessage(it, subject, text))
                .forEach(mailSender::sendMail);
    }

    private String createText(EventReport report) {
        String reportText = String.format(REPORT_FORMAT, report.success(), report.fail());

        return adminReportView.render(Map.of("report", reportText));
    }

    private MailMessage createMessage(Admin admin, String subject, String text) {
        return new MailMessage(admin.getEmail(), subject, text, adminReportView.getType());
    }
}
