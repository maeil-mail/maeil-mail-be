package maeilmail.subscribequestion;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class WeeklySubscribeQuestionSummary {

    private Long index;
    private final Long id;
    private final String title;

    @QueryProjection
    public WeeklySubscribeQuestionSummary(Long id, String title) {
        this.id = id;
        this.title = title;
    }
}
