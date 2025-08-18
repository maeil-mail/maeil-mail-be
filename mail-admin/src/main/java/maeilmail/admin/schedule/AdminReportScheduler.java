package maeilmail.admin.schedule;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.admin.domain.Admin;
import maeilmail.admin.domain.AdminRepository;
import maeilmail.admin.view.AdminReportView;
import maeilmail.mail.MailSender;
import maeilmail.mail.MailView;
import maeilmail.mail.MailViewRenderer;
import maeilmail.mail.SimpleMailMessage;
import maeilmail.statistics.DailySendReport;
import maeilmail.statistics.StatisticsService;
import maeilmail.utils.DistributedSupport;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class AdminReportScheduler {

    private final MailSender mailSender;
    private final AdminRepository adminRepository;
    private final DistributedSupport distributedSupport;
    private final StatisticsService statisticsService;
    private final MailViewRenderer mailViewRenderer;

    @Scheduled(cron = "0 40 7 * * MON-FRI", zone = "Asia/Seoul")
    public void sendReport() {
        LocalDate today = LocalDate.now();
        log.info("관리자 결과 전송, date = {}", today);
        DailySendReport dailySendReport = statisticsService.generateDailySendReport(today);

        MailView view = createView(dailySendReport);
        String subject = "관리자 전용 메일 전송 결과를 알려드립니다.";
        String text = view.render();

        adminRepository.findAll().stream()
                .filter(it -> distributedSupport.isMine(it.getId()))
                .map(it -> createMessage(it, subject, text, view))
                .forEach(mailSender::sendMail);
    }

    private MailView createView(DailySendReport report) {
        return AdminReportView.builder()
                .renderer(mailViewRenderer)
                .dailySendReport(report)
                .build();
    }

    private SimpleMailMessage createMessage(Admin admin, String subject, String text, MailView view) {
        return new SimpleMailMessage(admin.getEmail(), subject, text, view.getType());
    }
}
