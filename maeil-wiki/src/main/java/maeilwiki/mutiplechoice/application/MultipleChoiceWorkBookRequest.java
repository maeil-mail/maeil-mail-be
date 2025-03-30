package maeilwiki.mutiplechoice.application;

import java.util.List;
import maeilwiki.member.domain.Member;
import maeilwiki.mutiplechoice.domain.MultipleChoiceWorkbook;

public record MultipleChoiceWorkBookRequest(
        String workBookTitle,
        int difficultyLevel,
        String category,
        String workbookDetail,
        Integer timeLimit,
        List<MultipleChoiceQuestionRequest> questions
) {

    public MultipleChoiceWorkbook toWorkBook(Member member) {
        return new MultipleChoiceWorkbook(workBookTitle, difficultyLevel, category, workbookDetail, timeLimit, member);
    }
}
