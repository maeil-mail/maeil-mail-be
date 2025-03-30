package maeilwiki.mutiplechoice.application;

import java.util.List;
import maeilwiki.mutiplechoice.domain.MultipleChoiceQuestion;
import maeilwiki.mutiplechoice.domain.MultipleChoiceWorkbook;

public record MultipleChoiceQuestionRequest(
        String title,
        String correctAnswerExplanation,
        List<MultipleChoiceOptionRequest> options
) {

    public MultipleChoiceQuestion toQuestion(MultipleChoiceWorkbook workbook) {
        return new MultipleChoiceQuestion(title, correctAnswerExplanation, workbook);
    }
}
