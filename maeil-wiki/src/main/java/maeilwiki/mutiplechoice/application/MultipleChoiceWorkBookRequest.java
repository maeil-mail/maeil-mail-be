package maeilwiki.mutiplechoice.application;

import java.util.List;

public record MultipleChoiceWorkBookRequest(
        String workBookTitle,
        int difficultyLevel,
        String category,
        String workbookDetail,
        Integer timeLimit,
        List<MultipleChoiceQuestionRequest> questions
) {
}
