package maeilwiki.mutiplechoice.application;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import maeilwiki.member.dto.MemberThumbnail;
import maeilwiki.mutiplechoice.dto.WorkbookSummary;

public record WorkbookResponse(
        @JsonInclude(Include.NON_NULL) Long id,
        String workbookTitle,
        int difficultyLevel,
        String category,
        String workbookDetail,
        MemberThumbnail owner,
        LocalDateTime createdAt,
        @JsonInclude(Include.NON_NULL) Integer timeLimit,
        int questionCount,
        int solvedCount,
        @JsonInclude(Include.NON_NULL) List<QuestionResponse> questions
) {

    public static WorkbookResponse withQuestions(WorkbookSummary workbookSummary, List<QuestionResponse> questionResponses) {
        return new WorkbookResponse(
                null,
                workbookSummary.workbookTitle(),
                workbookSummary.difficultyLevel(),
                workbookSummary.category(),
                workbookSummary.workbookDetail(),
                workbookSummary.owner(),
                workbookSummary.createdAt(),
                workbookSummary.timeLimit(),
                questionResponses.size(),
                workbookSummary.solvedCount(),
                questionResponses
        );
    }

    public static WorkbookResponse withQuestionCount(WorkbookSummary workbookSummary, Long questionCount) {
        return new WorkbookResponse(
                workbookSummary.id(),
                workbookSummary.workbookTitle(),
                workbookSummary.difficultyLevel(),
                workbookSummary.category(),
                workbookSummary.workbookDetail(),
                workbookSummary.owner(),
                workbookSummary.createdAt(),
                null,
                Math.toIntExact(questionCount),
                workbookSummary.solvedCount(),
                null
        );
    }
}
