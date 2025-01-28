package maeilwiki.wiki.dto;

import java.time.LocalDateTime;
import com.querydsl.core.annotations.QueryProjection;
import maeilwiki.member.dto.MemberThumbnail;

public record WikiSummary(
        Long id,
        String question,
        String questionDetail,
        String category,
        boolean isAnonymous,
        MemberThumbnail owner,
        LocalDateTime createdAt
) {

    @QueryProjection
    public WikiSummary {
    }
}
