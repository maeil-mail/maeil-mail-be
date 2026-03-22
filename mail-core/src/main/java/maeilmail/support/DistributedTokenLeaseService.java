package maeilmail.support;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.Instant;
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
class DistributedTokenLeaseService {

    private final String bucketKey;
    private final BucketConfiguration bucketConfiguration;

    private final MySQLSelectForUpdateBasedProxyManager<Long> proxyManager;
    private final ConcurrentMap<String, BucketProxy> buckets = new ConcurrentHashMap<>();

    public DistributedTokenLeaseService(
            DataSource dataSource,
            @Value("${mail.ses.rate-limit.bucket-key}") String bucketKey,
            @Value("${mail.ses.rate-limit.capacity}") int capacity,
            @Value("${mail.ses.rate-limit.refill-amount}") int refillAmount,
            @Value("${mail.ses.rate-limit.refill-seconds}") long refillSeconds
    ) {
        this.bucketKey = bucketKey;
        this.bucketConfiguration = createConfiguration(capacity, refillAmount, Duration.ofSeconds(refillSeconds));

        BucketTableSettings tableSettings = BucketTableSettings.getDefault();
        SQLProxyConfiguration<Long> sqlProxyConfiguration = SQLProxyConfiguration.builder()
                .withTableSettings(tableSettings)
                .build(dataSource);
        this.proxyManager = new MySQLSelectForUpdateBasedProxyManager<>(sqlProxyConfiguration);
    }

    private BucketConfiguration createConfiguration(int capacity, int refillAmount, Duration refillDuration) {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervallyAligned(refillAmount, refillDuration, Instant.EPOCH)
                .build();

        return BucketConfiguration.builder()
                .addLimit(bandwidth)
                .build();
    }

    public boolean tryLeaseToken(int leaseAmount) {
        BucketProxy bucket = getOrCreateBucket(bucketKey, bucketConfiguration);
        boolean consumed = bucket.tryConsume(leaseAmount);
        log.debug("SES 처리율 제한 버킷 key={}, consumed={}", bucketKey, consumed);

        return consumed;
    }

    private BucketProxy getOrCreateBucket(String key, BucketConfiguration configuration) {
        return buckets.computeIfAbsent(key, bucketKey -> {
            long bucketId = bucketKey.hashCode();
            return proxyManager.builder()
                    .build(bucketId, configuration);
        });
    }
}
