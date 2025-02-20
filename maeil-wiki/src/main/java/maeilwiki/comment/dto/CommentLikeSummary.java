package maeilwiki.comment.dto;

import com.querydsl.core.annotations.QueryProjection;

public record CommentLikeSummary(Long memberId) {

    @QueryProjection
    public CommentLikeSummary {
    }
}
