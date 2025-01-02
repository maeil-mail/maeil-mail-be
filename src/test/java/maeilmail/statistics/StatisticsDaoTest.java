package maeilmail.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.util.Map;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(value = "/counting.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class StatisticsDaoTest extends IntegrationTestSupport {

    @Autowired
    private StatisticsDao statisticsDao;

    @Test
    @DisplayName("특정 날짜에 대한 성공 실패 카운트를 조회한다.")
    void querySuccessFailCount() {
        LocalDateTime monday = LocalDateTime.of(2024, 12, 30, 7, 0, 0);
        LocalDateTime tuesday = LocalDateTime.of(2024, 12, 31, 7, 0, 0);

        Map<Boolean, Long> actualMonday = statisticsDao.querySuccessFailCount(monday);
        Map<Boolean, Long> actualTuesday = statisticsDao.querySuccessFailCount(tuesday);

        assertAll(
                () -> assertThat(actualMonday.get(Boolean.TRUE)).isEqualTo(5),
                () -> assertThat(actualMonday.get(Boolean.FALSE)).isEqualTo(2),
                () -> assertThat(actualTuesday.get(Boolean.TRUE)).isEqualTo(8),
                () -> assertThat(actualTuesday.get(Boolean.FALSE)).isEqualTo(null)
        );
    }

    @Test
    @DisplayName("구독 주기별 전송 대상의 수를 반환한다.")
    void querySubscribeCountForFrequency() {
        LocalDateTime monday = LocalDateTime.of(2024, 12, 30, 7, 0, 0);
        LocalDateTime tuesday = LocalDateTime.of(2024, 12, 31, 7, 0, 0);

        Map<SubscribeFrequency, Long> actualMonday = statisticsDao.querySubscribeCountForFrequency(monday);
        Map<SubscribeFrequency, Long> actualTuesday = statisticsDao.querySubscribeCountForFrequency(tuesday);

        assertAll(
                () -> assertThat(actualMonday.get(SubscribeFrequency.DAILY)).isEqualTo(7),
                () -> assertThat(actualMonday.get(SubscribeFrequency.WEEKLY)).isEqualTo(1),
                () -> assertThat(actualTuesday.get(SubscribeFrequency.DAILY)).isEqualTo(8),
                () -> assertThat(actualTuesday.get(SubscribeFrequency.WEEKLY)).isEqualTo(1)
        );
    }
}
