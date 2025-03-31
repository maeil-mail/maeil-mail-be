package maeilwiki.mutiplechoice.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TimeLimitTest {

    @Test
    @DisplayName("시간 제한은 필수값이 아니다.")
    void allowNull() {
        assertThatCode(() -> new TimeLimit(null))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 15, 20, 25, 30, 40, 50, 60})
    @DisplayName("값은 [5, 10, 15, 20, 25, 30, 40, 50, 60] 중에 하나여야 한다. (해피 케이스)")
    void validateTimeLimit(int source) {
        assertThatCode(() -> new TimeLimit(source))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(ints = {6, 11, 16, 21, 26, 31, 41, 51, 61})
    @DisplayName("값은 [5, 10, 15, 20, 25, 30, 40, 50, 60] 중에 하나여야 한다. (예외 케이스)")
    void validateTimeLimitException(int source) {
        assertThatThrownBy(() -> new TimeLimit(source))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 시간 제한입니디.");
    }
}
