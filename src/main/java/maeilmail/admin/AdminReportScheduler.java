package maeilmail.admin;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.DistributedSupport;
import maeilmail.mail.MailEvent;
import maeilmail.mail.MailEventRepository;
import maeilmail.mail.MailMessage;
import maeilmail.mail.MailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class AdminReportScheduler {

    private final MailEventRepository mailEventRepository;
    private final AdminRepository adminRepository;
    private final AdminReportView adminReportView;
    private final MailSender mailSender;
    private final DistributedSupport distributedSupport;

    @Scheduled(cron = "0 30 7 1/1 * ?", zone = "Asia/Seoul")
    public void sendReport() {
        LocalDate now = LocalDate.now();
        log.info("관리자 결과 전송, date = {}", now);

        List<MailEvent> result = mailEventRepository.findMailEventByDate(now);
        AdminReport adminReport = new AdminReport(result);
        String report = adminReport.generateReport("question");
        String text = createText(report);
        String subject = "[관리자] 메일 전송 결과를 알려드립니다.";

        adminRepository.findAll().stream()
                .filter(it -> distributedSupport.isMine(it.getId()))
                .map(it -> createMessage(it, subject, text))
                .forEach(mailSender::sendMail);
    }

    private String createText(String report) {
        return adminReportView.render(Map.of("report", report));
    }

    private MailMessage createMessage(Admin admin, String subject, String text) {
        return new MailMessage(admin.getEmail(), subject, text, adminReportView.getType());
    }
}
