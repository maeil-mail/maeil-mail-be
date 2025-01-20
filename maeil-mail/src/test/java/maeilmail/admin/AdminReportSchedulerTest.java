package maeilmail.admin;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import maeilmail.support.SchedulerTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdminReportSchedulerTest {

    @Test
    @DisplayName("매주 월요일부터 금요일까지 평일에 한해서 매일 아침 7시 30분에 관리자 리포트 스케줄러가 동작하는지 확인한다.")
    void sendReportCronWeekday() {
        LocalDateTime initialTime = LocalDateTime.of(2024, 8, 26, 7, 30); // 월요일
        List<LocalDateTime> expectedTimes = List.of(
                LocalDateTime.of(2024, 8, 27, 7, 30),  // 화요일
                LocalDateTime.of(2024, 8, 28, 7, 30),  // 수요일
                LocalDateTime.of(2024, 8, 29, 7, 30),  // 목요일
                LocalDateTime.of(2024, 8, 30, 7, 30),  // 금요일
                LocalDateTime.of(2024, 9, 2, 7, 30),   // 다음 주 월요일
                LocalDateTime.of(2024, 9, 3, 7, 30)    // 다음 주 화요일
        );
        SchedulerTestUtils.assertCronExpression(
                AdminReportScheduler.class,
                "sendReport",
                toInstant(initialTime),
                expectedTimes.stream().map(this::toInstant).toList()
        );
    }

    private Instant toInstant(LocalDateTime time) {
        return time.atZone(ZoneId.of("Asia/Seoul")).toInstant();
    }
}
