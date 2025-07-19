package maeilwiki.wiki.dto;

import com.querydsl.core.annotations.QueryProjection;

public record WikiSummaryWithCommentCount(
        WikiSummary wikiSummary,
        Long commentCount
) {

    @QueryProjection
    public WikiSummaryWithCommentCount {
    }
}
