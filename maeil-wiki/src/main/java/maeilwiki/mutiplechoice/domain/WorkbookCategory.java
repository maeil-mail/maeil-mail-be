package maeilwiki.mutiplechoice.domain;

import java.util.Arrays;
import java.util.NoSuchElementException;

public enum WorkbookCategory {

    FRONTEND, BACKEND, ETC;

    public static WorkbookCategory from(String category) {
        return Arrays.stream(WorkbookCategory.values())
                .filter(it -> it.name().equalsIgnoreCase(category))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }
}
