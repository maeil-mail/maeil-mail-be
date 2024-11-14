package maeilmail.admin;

import java.time.LocalDateTime;

public record AdminNoticeRequest(
        String title,
        String content,
        LocalDateTime reservedTime
) {
}
