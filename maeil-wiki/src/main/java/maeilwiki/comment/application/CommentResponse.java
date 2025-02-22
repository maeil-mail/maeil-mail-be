package maeilwiki.comment.application;

import maeilwiki.comment.dto.CommentSummary;
import maeilwiki.member.dto.MemberThumbnail;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String answer,
        LocalDateTime createdAt,
        boolean isLiked,
        long likeCount,
        MemberThumbnail owner
) {

    public static CommentResponse of(CommentSummary commentSummary) {
        return new CommentResponse(
                commentSummary.id(),
                commentSummary.answer(),
                commentSummary.createdAt(),
                commentSummary.isLiked(),
                commentSummary.likeCount(),
                commentSummary.owner()
        );
    }
}
