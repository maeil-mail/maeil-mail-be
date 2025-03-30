package maeilwiki.mutiplechoice.domain;

import java.util.List;

public class Questions {

    private final List<Question> questions;
    private final List<Options> options;

    public Questions(List<Question> questions, List<Options> options) {
        validateSize(questions);
        this.questions = questions;
        this.options = options;
    }

    private void validateSize(List<Question> questions) {
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("객관식 질문은 최소 1개 이상이어야 합니다.");
        }
    }
}
