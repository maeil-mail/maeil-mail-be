package maeilwiki.mutiplechoice.dto;

import com.querydsl.core.annotations.QueryProjection;

public record OptionSummary(
        Long id,
        Long questionId,
        String content,
        boolean isCorrectAnswer
) {

    @QueryProjection
    public OptionSummary {
    }
}
