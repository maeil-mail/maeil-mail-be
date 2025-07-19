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
        LocalDateTime createdAt,
        MemberThumbnail owner
) {

    @QueryProjection
    public WikiSummary {
    }

    public WikiSummary toAnonymousOwner() {
        MemberThumbnail anonymousOwner = new MemberThumbnail(owner.id(), null, null, null);

        return new WikiSummary(id, question, questionDetail, category, isAnonymous, createdAt, anonymousOwner);
    }
}
