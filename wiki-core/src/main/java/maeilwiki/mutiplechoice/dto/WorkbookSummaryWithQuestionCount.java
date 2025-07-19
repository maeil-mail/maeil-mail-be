package maeilwiki.mutiplechoice.dto;

import com.querydsl.core.annotations.QueryProjection;

public record WorkbookSummaryWithQuestionCount(
        WorkbookSummary workbookSummary,
        Long questionCount
) {

    @QueryProjection
    public WorkbookSummaryWithQuestionCount {
    }
}
