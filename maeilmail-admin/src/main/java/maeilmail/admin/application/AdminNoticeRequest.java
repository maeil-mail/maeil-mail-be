package maeilmail.admin.application;

import java.time.LocalDate;
import maeilmail.admin.domain.AdminNotice;

public record AdminNoticeRequest(
        Long id,
        String title,
        String content,
        LocalDate date
) {

    public AdminNotice toAdminNotice() {
        return new AdminNotice(id, title, content, date);
    }

    public boolean isUpdate() {
        return id != null;
    }
}
