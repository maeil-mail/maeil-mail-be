package maeilmail.subscribe;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SendQuestionSchedulerTest {

    @Test
    @DisplayName("매일 아침 7시에 스케줄러가 동작하는지 확인한다.")
    void cronTest() {
        LocalDateTime initialTime = LocalDateTime.of(2024, 8, 26, 7, 0);
        List<LocalDateTime> expectedTimes = List.of(
                LocalDateTime.of(2024, 8, 27, 7, 0),
                LocalDateTime.of(2024, 8, 28, 7, 0),
                LocalDateTime.of(2024, 8, 29, 7, 0),
                LocalDateTime.of(2024, 8, 30, 7, 0),
                LocalDateTime.of(2024, 8, 31, 7, 0),
                LocalDateTime.of(2024, 9, 1, 7, 0)
        );
        SchedulerTestUtils.assertCronExpression(
                SendQuestionScheduler.class,
                "sendMail",
                toInstant(initialTime),
                expectedTimes.stream().map(this::toInstant).toList()
        );
    }

    private Instant toInstant(LocalDateTime time) {
        return time.atZone(ZoneId.of("Asia/Seoul")).toInstant();
    }
}
