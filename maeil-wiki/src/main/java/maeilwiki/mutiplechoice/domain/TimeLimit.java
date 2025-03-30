package maeilwiki.mutiplechoice.domain;

import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeLimit {

    private static final List<Integer> TIME_TABLE = List.of(5, 10, 15, 20, 25, 30, 40, 50, 60);

    @Column(columnDefinition = "INT")
    private Integer timeLimit;

    public TimeLimit(Integer timeLimit) {
        if (isInvalidTime(timeLimit)) {
            throw new IllegalArgumentException("값은 %s 중에 하나여야 합니다.".formatted(TIME_TABLE.toString()));
        }

        this.timeLimit = timeLimit;
    }

    private boolean isInvalidTime(Integer timeLimit) {
        return timeLimit != null && !TIME_TABLE.contains(timeLimit);
    }
}
