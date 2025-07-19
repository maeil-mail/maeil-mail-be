package maeilwiki.mutiplechoice.domain;

import java.util.List;

public record Questions(List<WorkbookQuestion> questions, List<Options> options) {

    public Questions {
        validateSize(questions);
    }

    private void validateSize(List<WorkbookQuestion> questions) {
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("객관식 질문은 최소 1개 이상이어야 합니다.");
        }
    }
}
