package maeilwiki.mutiplechoice.application;

import java.time.LocalDateTime;
import java.util.List;
import maeilwiki.member.dto.MemberThumbnail;
import maeilwiki.mutiplechoice.dto.WorkbookSummary;

public record WorkbookResponse(
        String workbookTitle,
        int difficultyLevel,
        String category,
        String workbookDetail,
        MemberThumbnail owner,
        LocalDateTime createdAt,
        Integer timeLimit,
        int questionCount,
        int solvedCount,
        List<QuestionResponse> questions
) {

    public static WorkbookResponse withQuestions(WorkbookSummary workbookSummary, List<QuestionResponse> questionResponses) {
        return new WorkbookResponse(
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
}
