package maeilwiki.mutiplechoice.application;

import java.util.List;

public record MultipleChoiceWorkBookRequest(
        String workBookTitle,
        Long difficultyLevel,
        String category,
        String workbookDetail,
        Long timeLimit,
        List<MultipleChoiceQuestionRequest> questions
) {
}
