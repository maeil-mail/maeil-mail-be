package maeilmail.admin;

import java.time.LocalDate;

record AdminNoticeResponse(
        Long id,
        String title,
        String content,
        LocalDate date
) {
}
