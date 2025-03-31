package maeilwiki.mutiplechoice.domain;

import java.util.List;

public record Options(List<Option> options) {

    public Options {
        validateSize(options);
    }

    private void validateSize(List<Option> options) {
        if (options.isEmpty()) {
            throw new IllegalArgumentException("객관식 항목은 최소 1개 이상이어야 합니다.");
        }
    }

    public int size() {
        return options.size();
    }
}
