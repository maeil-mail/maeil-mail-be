package maeilwiki.mutiplechoice.application;

import java.util.List;
import maeilwiki.mutiplechoice.domain.Question;
import maeilwiki.mutiplechoice.domain.Workbook;

public record QuestionRequest(
        String title,
        String correctAnswerExplanation,
        List<OptionRequest> options
) {

    public Question toQuestion(Workbook workbook) {
        return new Question(title, correctAnswerExplanation, workbook);
    }
}
