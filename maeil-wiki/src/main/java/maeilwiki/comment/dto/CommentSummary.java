package maeilwiki.comment.dto;

import com.querydsl.core.annotations.QueryProjection;
import maeilwiki.member.dto.MemberThumbnail;

import java.time.LocalDateTime;

public record CommentSummary(
        Long id,
        String answer,
        boolean isAnonymous,
        boolean isLiked,
        Long likeCount,
        LocalDateTime createdAt,
        MemberThumbnail owner
) {

    @QueryProjection
    public CommentSummary {
    }

    public CommentSummary toAnonymousOwner() {
        MemberThumbnail anonymousOwner = new MemberThumbnail(owner.id(), null, null, null);

        return new CommentSummary(id, answer, isAnonymous, isLiked, likeCount, createdAt, anonymousOwner);
    }
}
