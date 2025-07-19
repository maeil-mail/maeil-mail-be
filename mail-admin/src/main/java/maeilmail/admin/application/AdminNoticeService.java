package maeilmail.admin.application;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import maeilmail.admin.domain.AdminNotice;
import maeilmail.admin.domain.AdminNoticeRepository;
import maeilmail.mail.MailMessage;
import maeilmail.mail.MailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminNoticeService {

    private final AdminNoticeRepository adminNoticeRepository;
    private final MailSender mailSender;

    @Transactional
    public void createNotice(AdminNotice notice) {
        adminNoticeRepository.save(notice);
    }

    @Transactional
    public void updateNotice(AdminNotice notice) {
        AdminNotice found = findNotice(notice.getId());

        found.setTitle(notice.getTitle());
        found.setContent(notice.getContent());
        found.setReservedAt(notice.getReservedAt());
    }

    public void sendTest(Long id, AdminNoticeTestRequest request) {
        AdminNotice adminNotice = findNotice(id);
        mailSender.sendMail(new MailMessage(request.target(), adminNotice.getTitle(), adminNotice.getContent(), "notice test"));
    }

    @Transactional
    public void deleteNotice(Long id) {
        adminNoticeRepository.delete(findNotice(id));
    }

    private AdminNotice findNotice(Long id) {
        return adminNoticeRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
    }
}
