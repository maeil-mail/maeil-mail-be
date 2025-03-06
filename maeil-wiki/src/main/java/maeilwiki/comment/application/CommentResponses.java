package maeilwiki.comment.application;

import maeilwiki.comment.dto.CommentSummary;

import java.util.List;

public record CommentResponses(
        List<CommentResponse> commentResponses,
        Long totalCount
) {

    public static CommentResponses of(List<CommentSummary> commentSummaries, Long totalCount) {
        List<CommentResponse> commentResponses = commentSummaries.stream()
                .map(CommentResponse::of)
                .toList();

        return new CommentResponses(commentResponses, totalCount);
    }
}
