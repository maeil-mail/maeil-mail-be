package maeilmail.support;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.jdbc.BucketTableSettings;
import io.github.bucket4j.distributed.jdbc.SQLProxyConfiguration;
import io.github.bucket4j.mysql.MySQLSelectForUpdateBasedProxyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RateLimiter {

    private final String bucketKey;
    private final Duration waitTimeout;
    private final BucketConfiguration bucketConfiguration;

    private final MySQLSelectForUpdateBasedProxyManager<Long> proxyManager;
    private final ConcurrentMap<String, BucketProxy> buckets = new ConcurrentHashMap<>();

    public RateLimiter(
            DataSource dataSource,
            @Value("${mail.ses.rate-limit.bucket-key}") String bucketKey,
            @Value("${mail.ses.rate-limit.capacity}") int capacity,
            @Value("${mail.ses.rate-limit.refill-amount}") int refillAmount,
            @Value("${mail.ses.rate-limit.refill-seconds}") long refillSeconds,
            @Value("${mail.ses.rate-limit.wait-timeout-millis}") long waitTimeoutMillis
    ) {
        this.bucketKey = bucketKey;
        this.waitTimeout = Duration.ofMillis(waitTimeoutMillis);
        this.bucketConfiguration = createConfiguration(capacity, refillAmount, Duration.ofSeconds(refillSeconds));

        BucketTableSettings tableSettings = BucketTableSettings.getDefault();
        SQLProxyConfiguration<Long> sqlProxyConfiguration = SQLProxyConfiguration.builder()
                .withTableSettings(tableSettings)
                .build(dataSource);
        this.proxyManager = new MySQLSelectForUpdateBasedProxyManager<>(sqlProxyConfiguration);
    }

    private BucketConfiguration createConfiguration(int capacity, int refillAmount, Duration refillDuration) {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillIntervally(refillAmount, refillDuration)
                        .build())
                .build();
    }

    public void tryConsume() {
        if (!tryConsume(bucketKey, bucketConfiguration, waitTimeout)) {
            throw new IllegalStateException("SES 처리율 제한을 초과했습니다.");
        }
    }

    private boolean tryConsume(String key, BucketConfiguration configuration, Duration waitTimeout) {
        BucketProxy bucket = getOrCreateBucket(key, configuration);
        try {
            boolean consumed = bucket.asBlocking().tryConsume(1, waitTimeout);
            log.debug("SES 처리율 제한 버킷 key={}, consumed={}", key, consumed);
            return consumed;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private BucketProxy getOrCreateBucket(String key, BucketConfiguration configuration) {
        return buckets.computeIfAbsent(key, bucketKey -> {
            long bucketId = bucketKey.hashCode();
            return proxyManager.builder()
                    .build(bucketId, configuration);
        });
    }
}
