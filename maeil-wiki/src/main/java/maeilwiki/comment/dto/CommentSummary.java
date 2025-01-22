package maeilwiki.comment.dto;

import java.time.LocalDateTime;
import com.querydsl.core.annotations.QueryProjection;
import maeilwiki.member.dto.MemberThumbnail;

public record CommentSummary(
        Long id,
        String answer,
        MemberThumbnail owner,
        LocalDateTime createdAt
) {

    @QueryProjection
    public CommentSummary {
    }
}
