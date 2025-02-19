package maeilmail.bulksend.schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.admin.AdminNotice;
import maeilmail.admin.AdminNoticeRepository;
import maeilmail.mail.MailMessage;
import maeilmail.mail.MailSender;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import maeilmail.subscribe.query.SubscribeEmail;
import maeilmail.support.DistributedSupport;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendAdminNoticeScheduler {

    private final MailSender mailSender;
    private final SubscribeRepository subscribeRepository;
    private final AdminNoticeRepository adminNoticeRepository;
    private final DistributedSupport distributedSupport;

    @Scheduled(cron = "0 0 8 * * MON", zone = "Asia/Seoul")
    public void sendMail() {
        Optional<AdminNotice> optionalAdminNotice = adminNoticeRepository.findByReservedAt(LocalDate.now());
        if (optionalAdminNotice.isEmpty()) {
            log.info("오늘 발송할 공지가 없습니다.");
            return;
        }
        AdminNotice adminNotice = optionalAdminNotice.get();

        log.info("공지 전송을 시작합니다.");
        List<SubscribeEmail> subscribes = subscribeRepository.findAllWithUniqueEmail();

        log.info("{}명의 구독자에게 공지 메일을 발송합니다.", subscribes.size());

        subscribes.stream()
                .filter(it -> distributedSupport.isMine(it.id()))
                .map(it -> new MailMessage(it.email(), adminNotice.getTitle(), adminNotice.getContent(), "notice"))
                .forEach(mailSender::sendMail);

        log.info("공지 전송을 종료합니다.");
    }
}
