package maeilmail.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DateUtilsTest {

    @Test
    @DisplayName("주어진 날짜 주차의 시작일을 반환한다.")
    void getMondayAt() {
        LocalDate 이번달_첫_주차 = DateUtils.getMondayAt(2024L, 12L, 1L);
        LocalDate 이번달_마지막_주차 = DateUtils.getMondayAt(2024L, 12L, 5L);
        LocalDate 다음달_첫_주차 = DateUtils.getMondayAt(2025L, 1L, 1L);

        assertAll(
                () -> assertThat(이번달_첫_주차).isEqualTo(LocalDate.of(2024, 12, 2)),
                () -> assertThat(이번달_마지막_주차).isEqualTo(LocalDate.of(2024, 12, 30)),
                () -> assertThat(다음달_첫_주차).isEqualTo(LocalDate.of(2025, 1, 6))
        );
    }

    @Test
    @DisplayName("주어진 날짜가 몇 주차인지 반환한다.")
    void getWeekOfMonth() {
        LocalDate 이번달_첫_주차_월요일 = LocalDate.of(2024, 12, 2);
        LocalDate 이번달_2_주차_월요일 = LocalDate.of(2024, 12, 9);
        LocalDate 이번달_마지막_주차_월요일 = LocalDate.of(2024, 12, 30);
        LocalDate 다음달_첫_주차_월요일 = LocalDate.of(2025, 1, 6);

        assertAll(
                () -> assertThat(DateUtils.getWeekOfMonth(이번달_첫_주차_월요일)).isEqualTo(1),
                () -> assertThat(DateUtils.getWeekOfMonth(이번달_2_주차_월요일)).isEqualTo(2),
                () -> assertThat(DateUtils.getWeekOfMonth(이번달_마지막_주차_월요일)).isEqualTo(5),
                () -> assertThat(DateUtils.getWeekOfMonth(다음달_첫_주차_월요일)).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("주어진 날짜가 해당 월의 첫 월요일보다 이전날인 경우 몇 주차인지 계산할 수 없다.")
    void getWeekOfMonth2() {
        LocalDate 이번달_마지막_주차_수요일 = LocalDate.of(2025, 1, 1);

        assertThatThrownBy(() -> DateUtils.getWeekOfMonth(이번달_마지막_주차_수요일))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("주어진 날짜가 월요일인지 판단할 수 있다.")
    void isMonday() {
        LocalDate monday = LocalDate.of(2024, 12, 30);
        LocalDate notMonday = LocalDate.of(2024, 12, 31);

        assertAll(
                () -> assertThat(DateUtils.isMonday(monday)).isTrue(),
                () -> assertThat(DateUtils.isMonday(notMonday)).isFalse()
        );
    }
}
