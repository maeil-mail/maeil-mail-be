package maeilwiki.mutiplechoice.application;

import java.util.List;
import maeilwiki.mutiplechoice.domain.WorkbookQuestion;
import maeilwiki.mutiplechoice.domain.Workbook;

public record QuestionRequest(
        String title,
        String correctAnswerExplanation,
        List<OptionRequest> options
) {

    public WorkbookQuestion toQuestion(Workbook workbook) {
        return new WorkbookQuestion(title, correctAnswerExplanation, workbook);
    }
}
