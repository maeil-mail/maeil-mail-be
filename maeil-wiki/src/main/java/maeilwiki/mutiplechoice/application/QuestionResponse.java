package maeilwiki.mutiplechoice.application;

import java.util.List;
import maeilwiki.mutiplechoice.dto.WorkbookQuestionSummary;

public record QuestionResponse(
        Long id,
        String title,
        String correctAnswerExplanation,
        List<OptionResponse> options
) {

    public static QuestionResponse withOptions(
            WorkbookQuestionSummary questionSummary,
            List<OptionResponse> optionResponses
    ) {
        return new QuestionResponse(
                questionSummary.id(),
                questionSummary.title(),
                questionSummary.correctAnswerExplanation(),
                optionResponses
        );
    }
}
