package maeilwiki.member;

import java.util.Arrays;
import java.util.NoSuchElementException;
import lombok.Getter;

@Getter
public enum Provider {

    GITHUB("GH-%s");

    private final String providerIdPrefix;

    Provider(String providerIdPrefix) {
        this.providerIdPrefix = providerIdPrefix;
    }

    public static Provider from(String provider) {
        return Arrays.stream(Provider.values())
                .filter(it -> it.name().equalsIgnoreCase(provider))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }
}
