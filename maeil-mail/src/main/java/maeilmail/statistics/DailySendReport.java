package maeilmail.statistics;

import java.util.Map;
import java.util.function.BiFunction;
import maeilmail.subscribe.command.domain.SubscribeFrequency;

public record DailySendReport(Long expectedSendingCount, Long actualSendingCount, Long success, Long fail) {

    public static DailySendReport generateDailySendReport(
            Map<SubscribeFrequency, Long> subscribeCountForFrequency,
            Map<Boolean, Long> successFailCount,
            BiFunction<SubscribeFrequency, Long, Long> frequencyCountPolicy
    ) {
        Long expectedSendingCount = calculateExpectedSendingCount(subscribeCountForFrequency, frequencyCountPolicy);

        Long success = successFailCount.getOrDefault(true, 0L);
        Long fail = successFailCount.getOrDefault(false, 0L);

        return new DailySendReport(expectedSendingCount, success + fail, success, fail);
    }

    private static Long calculateExpectedSendingCount(Map<SubscribeFrequency, Long> subscribeCountForFrequency, BiFunction<SubscribeFrequency, Long, Long> frequencyCountPolicy) {
        subscribeCountForFrequency.compute(SubscribeFrequency.WEEKLY, frequencyCountPolicy);

        return subscribeCountForFrequency.values().stream()
                .reduce(0L, Long::sum);
    }
}
