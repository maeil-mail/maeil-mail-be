package maeilmail.subscribe.command.domain;

import java.util.Arrays;
import java.util.NoSuchElementException;

public enum SubscribeFrequency {

    DAILY(1), WEEKLY(5);

    private final int sendCount;

    SubscribeFrequency(int sendCount) {
        this.sendCount = sendCount;
    }

    public static SubscribeFrequency from(String frequency) {
        return Arrays.stream(SubscribeFrequency.values())
                .filter((it) -> it.name().equalsIgnoreCase(frequency))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    public String toLowerCase() {
        return this.name().toLowerCase();
    }

    public int getSendCount() {
        return sendCount;
    }
}
