package maeilwiki.mutiplechoice.application;

import java.util.List;

public record MultipleChoiceQuestionRequest(
        String title,
        String correctAnswerExplanation,
        List<MultipleChoiceOptionRequest> options
) {
}
