package maeilwiki.comment.application;

import java.time.LocalDateTime;
import maeilwiki.comment.dto.CommentSummary;
import maeilwiki.member.dto.MemberThumbnail;

public record CommentResponse(
        Long id,
        String answer,
        LocalDateTime createdAt,
        MemberThumbnail owner
) {

    public static CommentResponse from(CommentSummary commentSummary) {
        return new CommentResponse(
                commentSummary.id(),
                commentSummary.answer(),
                commentSummary.createdAt(),
                commentSummary.owner()
        );
    }
}
