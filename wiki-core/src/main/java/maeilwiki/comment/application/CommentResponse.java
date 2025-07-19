package maeilwiki.comment.application;

import java.time.LocalDateTime;
import maeilwiki.comment.dto.CommentSummary;
import maeilwiki.member.dto.MemberThumbnail;

public record CommentResponse(
        Long id,
        String answer,
        LocalDateTime createdAt,
        boolean isLiked,
        long likeCount,
        MemberThumbnail owner
) {

    public static CommentResponse of(CommentSummary commentSummary, boolean isLiked) {
        return new CommentResponse(
                commentSummary.id(),
                commentSummary.answer(),
                commentSummary.createdAt(),
                isLiked,
                commentSummary.likeCount(),
                commentSummary.owner()
        );
    }
}
