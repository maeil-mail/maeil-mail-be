package maeilmail.subscribe;

import java.util.Arrays;
import java.util.NoSuchElementException;

enum SubscribeFrequency {

    DAILY, WEEKLY;

    public static SubscribeFrequency from(String frequency) {
        return Arrays.stream(SubscribeFrequency.values())
                .filter((it) -> it.name().equalsIgnoreCase(frequency))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    public String toLowerCase() {
        return this.name().toLowerCase();
    }
}
