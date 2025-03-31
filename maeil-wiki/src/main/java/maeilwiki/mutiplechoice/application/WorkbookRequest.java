package maeilwiki.mutiplechoice.application;

import java.util.List;
import maeilwiki.member.domain.Member;
import maeilwiki.mutiplechoice.domain.Workbook;

public record WorkbookRequest(
        String workbookTitle,
        int difficultyLevel,
        String category,
        String workbookDetail,
        Integer timeLimit,
        List<QuestionRequest> questions
) {

    public Workbook toWorkbook(Member member) {
        return new Workbook(workbookTitle, difficultyLevel, category, workbookDetail, timeLimit, member);
    }
}
