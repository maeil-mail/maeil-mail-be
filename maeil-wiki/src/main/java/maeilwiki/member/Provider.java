package maeilwiki.member;

import java.util.Arrays;
import java.util.NoSuchElementException;

public enum Provider {

    GITHUB;

    public static Provider from(String provider) {
        return Arrays.stream(Provider.values())
                .filter(it -> it.name().equalsIgnoreCase(provider))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }
}
