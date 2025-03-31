package maeilwiki.mutiplechoice.domain;

import java.util.List;
import java.util.Optional;
import maeilwiki.mutiplechoice.dto.OptionSummary;
import maeilwiki.mutiplechoice.dto.WorkbookQuestionSummary;
import maeilwiki.mutiplechoice.dto.WorkbookSummary;

public interface WorkbookRepositoryCustom {

    Optional<WorkbookSummary> queryOneById(Long workbookId);

    List<WorkbookQuestionSummary> queryQuestionsByWorkbookId(Long workbookId);

    List<OptionSummary> queryOptionsByQuestionIdsIn(List<Long> questionIds);
}
