package maeilmail.admin.application;

import java.time.LocalDate;

public record AdminNoticeResponse(
        Long id,
        String title,
        String content,
        LocalDate date
) {
}
