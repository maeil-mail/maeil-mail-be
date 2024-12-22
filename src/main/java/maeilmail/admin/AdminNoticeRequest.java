package maeilmail.admin;

import java.time.LocalDate;

public record AdminNoticeRequest(
        Long id,
        String title,
        String content,
        LocalDate date
) {

    public static AdminNoticeRequest from(AdminNotice notice) {
        return new AdminNoticeRequest(notice.getId(), notice.getTitle(), notice.getContent(), notice.getReservedAt());
    }

    public AdminNotice toAdminNotice() {
        return new AdminNotice(id, title, content, date);
    }

    public boolean isUpdate() {
        return id != null;
    }
}
