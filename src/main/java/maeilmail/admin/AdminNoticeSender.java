package maeilmail.admin;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.mail.MailMessage;
import maeilmail.mail.MailSender;
import maeilmail.subscribe.SubscribeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminNoticeSender {

    private final MailSender mailSender;
    private final SubscribeRepository subscribeRepository;

    @Transactional
    public void sendNotice(AdminNoticeRequest request) {
        log.info("공지 전송을 시작합니다.");
        List<String> distinctEmails = subscribeRepository.findDistinctEmails();

        log.info("{}명의 구독자에게 공지 메일을 발송합니다.", distinctEmails.size());

        distinctEmails.stream()
                .map(it -> createNotice(it, request.title(), request.content()))
                .forEach(mailSender::sendMail);

        log.info("공지 전송을 종료합니다.");
    }

    private MailMessage createNotice(String email, String title, String content) {
        return new MailMessage(email, title, content, "notice");
    }
}
