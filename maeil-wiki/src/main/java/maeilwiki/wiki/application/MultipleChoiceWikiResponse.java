package maeilwiki.wiki.application;

import com.fasterxml.jackson.annotation.JsonInclude;
import maeilwiki.member.domain.Member;
import maeilwiki.member.dto.MemberThumbnail;
import maeilwiki.wiki.domain.MultipleChoiceWiki;
import maeilwiki.wiki.dto.MultipleChoiceWikiSummary;
import java.time.LocalDateTime;
import java.util.List;
import static com.fasterxml.jackson.annotation.JsonInclude.Include;

public record MultipleChoiceWikiResponse(
        Long id,
        String title,
        @JsonInclude(Include.NON_NULL) String content,
        String category,
        boolean isAnonymous,
        int difficultyLevel,
        List<MultipleChoiceQuestionResponse> multipleChoiceQuestions,
        LocalDateTime createdAt,
        MemberThumbnail owner
) {

    public static MultipleChoiceWikiResponse of(MultipleChoiceWiki wiki, Member member) {
        return new MultipleChoiceWikiResponse(
                wiki.getId(),
                wiki.getTitle(),
                wiki.getDetail(),
                wiki.getCategory().name(),
                wiki.isAnonymous(),
                wiki.getDifficultyLevel(),
                wiki.getMultipleChoiceQuestions().stream().map(it -> new MultipleChoiceQuestionResponse(it.getContent(), it.isAnswer())).toList(),
                wiki.getCreatedAt(),
                new MemberThumbnail(member.getId(), member.getName(), member.getProfileImageUrl(), member.getGithubUrl())
        );
    }

    public static MultipleChoiceWikiResponse of(MultipleChoiceWikiSummary summary) {
        return new MultipleChoiceWikiResponse(
                summary.id(),
                summary.title(),
                summary.detail(),
                summary.category(),
                summary.isAnonymous(),
                summary.difficultyLevel(),
                summary.multipleChoiceQuestions().stream().map(it -> new MultipleChoiceQuestionResponse(it.getContent(), it.isAnswer())).toList(),
                summary.createdAt(),
                summary.owner()
        );
    }

    record MultipleChoiceQuestionResponse(String content, boolean isAnswer) {
    }
}
