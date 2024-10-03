package maeilmail.admin;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.subscribe.EmailSender;
import maeilmail.subscribe.MailEvent;
import maeilmail.subscribe.MailEventRepository;
import maeilmail.subscribe.MailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class AdminReportScheduler {

    private final MailEventRepository mailEventRepository;
    private final AdminRepository adminRepository;
    private final AdminReportView adminReportView;
    private final EmailSender emailSender;

    @Scheduled(cron = "0 30 7 1/1 * ?", zone = "Asia/Seoul")
    public void sendReport() {
        log.info("관리자 결과 전송, date = {}", LocalDate.now());

        List<MailEvent> result = mailEventRepository.findMailEventByDate(LocalDate.now());
        AdminReport adminReport = new AdminReport(result);
        String report = adminReport.generateReport("question");
        String text = createText(report);
        String subject = "[관리자] 메일 전송 결과를 알려드립니다.";

        for (Admin admin : adminRepository.findAll()) {
            MailMessage mailMessage = new MailMessage(admin.getEmail(), subject, text, adminReportView.getType());
            emailSender.sendMail(mailMessage);
        }
    }

    private String createText(String report) {
        return adminReportView.render(Map.of("report", report));
    }
}
