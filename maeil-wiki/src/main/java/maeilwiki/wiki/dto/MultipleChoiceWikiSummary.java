package maeilwiki.wiki.dto;

import com.querydsl.core.annotations.QueryProjection;
import maeilwiki.member.dto.MemberThumbnail;
import maeilwiki.wiki.domain.MultipleChoiceQuestion;
import java.time.LocalDateTime;
import java.util.List;

public record MultipleChoiceWikiSummary(
        Long id,
        String title,
        String detail,
        String category,
        boolean isAnonymous,
        int difficultyLevel,
        List<MultipleChoiceQuestion> multipleChoiceQuestions,
        LocalDateTime createdAt,
        MemberThumbnail owner
) {

    @QueryProjection
    public MultipleChoiceWikiSummary {
    }

    public MultipleChoiceWikiSummary toAnonymousOwner() {
        MemberThumbnail anonymousOwner = new MemberThumbnail(owner.id(), null, null, null);

        return new MultipleChoiceWikiSummary(id, title, detail, category, isAnonymous, difficultyLevel, multipleChoiceQuestions, createdAt, anonymousOwner);
    }
}
