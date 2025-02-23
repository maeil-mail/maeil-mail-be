package maeilwiki.wiki.application;

import com.fasterxml.jackson.annotation.JsonInclude;
import maeilwiki.member.dto.MemberThumbnail;
import maeilwiki.wiki.dto.WikiSummary;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

public record WikiResponse(
        Long id,
        String question,
        @JsonInclude(Include.NON_NULL) String questionDetail,
        String category,
        MemberThumbnail owner,
        LocalDateTime createdAt,
        Long commentCount
) {

    public static WikiResponse of(WikiSummary wikiSummary, Long commentCount) {
        return new WikiResponse(
                wikiSummary.id(),
                wikiSummary.question(),
                wikiSummary.questionDetail(),
                wikiSummary.category(),
                wikiSummary.owner(),
                wikiSummary.createdAt(),
                commentCount
        );
    }
}
