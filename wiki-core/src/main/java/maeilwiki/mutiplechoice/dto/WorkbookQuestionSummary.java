package maeilwiki.mutiplechoice.dto;

import com.querydsl.core.annotations.QueryProjection;

public record WorkbookQuestionSummary(
        Long id,
        String title,
        String correctAnswerExplanation
) {

    @QueryProjection
    public WorkbookQuestionSummary {
    }
}
