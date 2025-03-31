package maeilwiki.mutiplechoice.application;

import java.util.List;

public record QuestionResponse(
        Long id,
        String title,
        String correctAnswerExplanation,
        List<OptionResponse> options
) {
}
