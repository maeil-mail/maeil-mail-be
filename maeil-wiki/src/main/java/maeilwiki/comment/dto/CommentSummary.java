package maeilwiki.comment.dto;

import java.time.LocalDateTime;
import java.util.Set;
import com.querydsl.core.annotations.QueryProjection;
import maeilwiki.member.dto.MemberThumbnail;

public record CommentSummary(
        Long id,
        String answer,
        boolean isAnonymous,
        LocalDateTime createdAt,
        Set<Long> likeMemberIds,
        MemberThumbnail owner
) {

    @QueryProjection
    public CommentSummary {
    }

    public CommentSummary toAnonymousOwner() {
        MemberThumbnail anonymousOwner = new MemberThumbnail(owner.id(), null, null, null);

        return new CommentSummary(id, answer, isAnonymous, createdAt, likeMemberIds, anonymousOwner);
    }

    public boolean isLikedBy(Long memberId) {
        return likeMemberIds.contains(memberId);
    }

    public long likeCount() {
        return likeMemberIds.size();
    }
}
