package maeilwiki.mutiplechoice.dto;

import java.time.LocalDateTime;
import com.querydsl.core.annotations.QueryProjection;
import maeilwiki.member.dto.MemberThumbnail;

public record WorkbookSummary(
        Long id,
        String workbookTitle,
        int difficultyLevel,
        String category,
        String workbookDetail,
        MemberThumbnail owner,
        LocalDateTime createdAt,
        Integer timeLimit,
        int solvedCount
) {

    @QueryProjection
    public WorkbookSummary {
    }
}
