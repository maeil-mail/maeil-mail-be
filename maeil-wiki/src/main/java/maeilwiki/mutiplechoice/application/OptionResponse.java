package maeilwiki.mutiplechoice.application;

import maeilwiki.mutiplechoice.dto.OptionSummary;

public record OptionResponse(
        Long id,
        String content,
        boolean isCorrectAnswer
) {

    public static OptionResponse from(OptionSummary optionSummary) {
        return new OptionResponse(optionSummary.id(), optionSummary.content(), optionSummary.isCorrectAnswer());
    }
}
