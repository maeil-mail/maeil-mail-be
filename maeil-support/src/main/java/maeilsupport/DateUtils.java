package maeilsupport;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

public class DateUtils {

    public static LocalDate getMondayAt(Long year, Long month, Long week) {
        LocalDate firstMonday = getFirstMonday(Math.toIntExact(year), Math.toIntExact(month));

        return firstMonday.plusWeeks(week - 1);
    }

    public static int getWeekOfMonth(LocalDate date) {
        LocalDate firstMonday = getFirstMonday(date.getYear(), date.getMonthValue());
        if (date.isBefore(firstMonday)) {
            throw new IllegalStateException("주어진 일자의 주차를 계산할 수 없습니다.");
        }

        return (int) ChronoUnit.WEEKS.between(firstMonday, date) + 1;
    }

    private static LocalDate getFirstMonday(int year, int month) {
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);

        return firstDayOfMonth.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
    }

    public static boolean isMonday(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        return dayOfWeek.equals(DayOfWeek.MONDAY);
    }
}
