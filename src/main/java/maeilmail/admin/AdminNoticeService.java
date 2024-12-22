package maeilmail.admin;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class AdminNoticeService {

    private final AdminNoticeRepository adminNoticeRepository;

    @Transactional
    public void createNotice(AdminNotice notice) {
        adminNoticeRepository.save(notice);
    }

    @Transactional
    public void updateNotice(AdminNotice notice) {
        AdminNotice found = adminNoticeRepository.findById(notice.getId())
                .orElseThrow(NoSuchElementException::new);

        found.setTitle(notice.getTitle());
        found.setContent(notice.getContent());
        found.setReservedAt(notice.getReservedAt());
    }
}
