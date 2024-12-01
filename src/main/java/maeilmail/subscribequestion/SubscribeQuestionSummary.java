package maeilmail.subscribequestion;

import com.querydsl.core.annotations.QueryProjection;

public record SubscribeQuestionSummary(Long id, String title, String category) {

    @QueryProjection
    public SubscribeQuestionSummary {
    }
}
