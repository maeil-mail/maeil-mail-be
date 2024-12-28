package maeilmail.subscribe.query;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class WeeklySubscribeQuestionSummary {

    private final Long id;
    private final Long index;
    private final String title;

    @QueryProjection
    public WeeklySubscribeQuestionSummary(Long id, String title) {
        this(id, 0L, title);
    }

    public WeeklySubscribeQuestionSummary(Long id, Long index, String title) {
        this.id = id;
        this.index = index;
        this.title = title;
    }
}
