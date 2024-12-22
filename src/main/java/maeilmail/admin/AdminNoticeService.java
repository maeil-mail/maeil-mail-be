package maeilmail.admin;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class AdminNoticeService {

    private final AdminNoticeRepository adminNoticeRepository;
    private final AdminNoticeSender adminNoticeSender;

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
        AdminNoticeRequest adminNoticeRequest = AdminNoticeRequest.from(findNotice(id));
        adminNoticeSender.sendOne(adminNoticeRequest, request.target());
    }

    private AdminNotice findNotice(Long id) {
        return adminNoticeRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
    }
}
