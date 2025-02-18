package maeilwiki.comment.dto;

import java.time.LocalDateTime;
import com.querydsl.core.annotations.QueryProjection;
import maeilwiki.member.dto.MemberThumbnail;

public record CommentSummary(
        Long id,
        String answer,
        boolean isAnonymous,
        LocalDateTime createdAt,
        Long likeCount,
        MemberThumbnail owner
) {

    @QueryProjection
    public CommentSummary {
    }

    public CommentSummary toAnonymousOwner() {
        MemberThumbnail anonymousOwner = new MemberThumbnail(owner.id(), null, null, null);

        return new CommentSummary(id, answer, isAnonymous, createdAt, likeCount, anonymousOwner);
    }
}
