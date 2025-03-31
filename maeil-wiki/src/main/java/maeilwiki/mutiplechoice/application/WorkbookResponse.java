package maeilwiki.mutiplechoice.application;

import java.time.LocalDateTime;
import java.util.List;
import maeilwiki.member.dto.MemberThumbnail;

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
}
