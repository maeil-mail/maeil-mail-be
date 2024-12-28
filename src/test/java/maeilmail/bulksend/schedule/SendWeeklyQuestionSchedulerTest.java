package maeilmail.bulksend.schedule;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import maeilmail.support.IntegrationTestSupport;
import maeilmail.support.SchedulerTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SendWeeklyQuestionSchedulerTest extends IntegrationTestSupport {

    @Test
    @DisplayName("주에 1번 주간 전송 스케줄러가 동작하는지 확인한다.")
    void sendMailCronWeekly() {
        LocalDateTime initialTime = LocalDateTime.of(2024, 12, 1, 7, 0); // 월요일
        List<LocalDateTime> expectedTimes = List.of(
                LocalDateTime.of(2024, 12, 2, 7, 0),    // 월요일
                LocalDateTime.of(2024, 12, 9, 7, 0),  // 월요일
                LocalDateTime.of(2024, 12, 16, 7, 0),  // 월요일
                LocalDateTime.of(2024, 12, 23, 7, 0),  // 월요일
                LocalDateTime.of(2024, 12, 30, 7, 0),  // 월요일
                LocalDateTime.of(2025, 1, 6, 7, 0)   // 월요일
        );
        SchedulerTestUtils.assertCronExpression(
                SendWeeklyQuestionScheduler.class,
                "sendMail",
                toInstant(initialTime),
                expectedTimes.stream().map(this::toInstant).toList()
        );
    }

    private Instant toInstant(LocalDateTime time) {
        return time.atZone(ZoneId.of("Asia/Seoul")).toInstant();
    }
}
