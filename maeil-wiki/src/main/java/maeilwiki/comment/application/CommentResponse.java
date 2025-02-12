package maeilwiki.comment.application;

import java.time.LocalDateTime;
import maeilwiki.comment.dto.CommentSummary;
import maeilwiki.member.dto.MemberThumbnail;

public record CommentResponse(
        Long id,
        String answer,
        LocalDateTime createdAt,
        Long likeCount,
        MemberThumbnail owner
) {

    public static CommentResponse from(CommentSummary commentSummary) {
        return new CommentResponse(
                commentSummary.id(),
                commentSummary.answer(),
                commentSummary.createdAt(),
                commentSummary.likeCount(),
                commentSummary.owner()
        );
    }
}
