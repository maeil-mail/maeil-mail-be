package maeilwiki.mutiplechoice.domain;

import java.util.Set;
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

    private static final Set<Integer> TIME_TABLE = Set.of(5, 10, 15, 20, 25, 30, 40, 50, 60);

    @Column(columnDefinition = "INT")
    private Integer timeLimit;

    public TimeLimit(Integer timeLimit) {
        if (isInvalidTime(timeLimit)) {
            throw new IllegalArgumentException("유효하지 않은 시간 제한입니디.");
        }

        this.timeLimit = timeLimit;
    }

    private boolean isInvalidTime(Integer timeLimit) {
        return timeLimit != null && !TIME_TABLE.contains(timeLimit);
    }
}
