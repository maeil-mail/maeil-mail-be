package maeilwiki.comment.dto;

import java.time.LocalDateTime;
import com.querydsl.core.annotations.QueryProjection;
import maeilwiki.member.dto.MemberThumbnail;

public record CommentSummary(
        Long id,
        String answer,
        boolean isAnonymous,
        LocalDateTime createdAt,
        MemberThumbnail owner
) {

    @QueryProjection
    public CommentSummary {
    }

    public CommentSummary toAnonymousOwner() {
        return new CommentSummary(id, answer, isAnonymous, createdAt, null);
    }
}
