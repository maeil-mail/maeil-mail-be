package maeilwiki.wiki;

import java.util.Arrays;

public enum WikiCategory {

    FRONTEND, BACKEND, ETC;

    public static WikiCategory from(String category) {
        return Arrays.stream(WikiCategory.values())
                .filter(it -> it.name().equalsIgnoreCase(category))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
