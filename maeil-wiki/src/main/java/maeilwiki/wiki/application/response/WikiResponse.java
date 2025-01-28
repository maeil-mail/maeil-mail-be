package maeilwiki.wiki.application.response;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import maeilwiki.comment.application.CommentResponse;
import maeilwiki.member.dto.MemberThumbnail;
import maeilwiki.wiki.dto.WikiSummary;

public record WikiResponse(
        Long id,
        String question,
        String questionDetail,
        String category,
        MemberThumbnail owner,
        LocalDateTime createdAt,
        @JsonInclude(Include.NON_NULL) List<CommentResponse> comments,
        @JsonInclude(Include.NON_NULL) Long commentCount
) {

    public static WikiResponse withComments(WikiSummary wikiSummary, List<CommentResponse> commentResponses) {
        return new WikiResponse(
                wikiSummary.id(),
                wikiSummary.question(),
                wikiSummary.questionDetail(),
                wikiSummary.category(),
                wikiSummary.owner(),
                wikiSummary.createdAt(),
                commentResponses,
                null
        );
    }
}
