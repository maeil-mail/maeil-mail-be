package maeilmail.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class DistributedRateLimitSupportIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${mail.ses.rate-limit.bucket-key}")
    private String bucketKey;

    @Value("${mail.ses.rate-limit.capacity}")
    private int capacity;

    @Value("${mail.ses.rate-limit.refill-seconds}")
    private long refillSeconds;

    private String testBucketKey;

    @BeforeEach
    void clearBucket() {
        jdbcTemplate.update("delete from bucket");
        testBucketKey = bucketKey + "-" + UUID.randomUUID();
    }

    @Test
    @DisplayName("여러 클라이언트가 토큰을 소비하더라도 전역 용량을 초과할 수 없다.")
    void cannotExceedGlobalCapacityAcrossClients() {
        waitUntilNextSecondBoundary();

        DistributedRateLimitSupport apiRateLimitSupport = createRateLimitSupport(5, 5);
        DistributedRateLimitSupport batchRateLimitSupport = createRateLimitSupport(5, 5);
        List<Boolean> consumedResults = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            consumedResults.add(apiRateLimitSupport.tryConsume());
        }
        boolean sixthConsumed = batchRateLimitSupport.tryConsume();

        assertAll(
                () -> assertThat(consumedResults).containsOnly(true),
                () -> assertThat(sixthConsumed).isFalse(),
                () -> assertThat(apiRateLimitSupport.getLeasedTokens()).isZero(),
                () -> assertThat(batchRateLimitSupport.getLeasedTokens()).isZero()
        );
    }

    @Test
    @DisplayName("다음 초 경계가 지나면 전역 토큰을 다시 임대받아 소비할 수 있다.")
    void consumesAgainAfterNextRefillBoundary() {
        waitUntilNextSecondBoundary();

        DistributedRateLimitSupport rateLimitSupport = createRateLimitSupport(5, 5);
        List<Boolean> consumedResults = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            consumedResults.add(rateLimitSupport.tryConsume());
        }
        boolean consumedBeforeRefill = rateLimitSupport.tryConsume();

        waitUntilNextSecondBoundary();

        boolean consumedAfterRefill = rateLimitSupport.tryConsume();

        assertAll(
                () -> assertThat(consumedResults).containsOnly(true),
                () -> assertThat(consumedBeforeRefill).isFalse(),
                () -> assertThat(consumedAfterRefill).isTrue(),
                () -> assertThat(rateLimitSupport.getLeasedTokens()).isEqualTo(4)
        );
    }

    @Test
    @DisplayName("timeout 안에 다음 초 경계가 오면 대기 후 토큰을 소비할 수 있다.")
    void consumesBlockingUntilNextRefillBoundary() {
        waitUntilNextSecondBoundary();

        DistributedRateLimitSupport rateLimitSupport = createRateLimitSupport();
        List<Boolean> consumedResults = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            consumedResults.add(rateLimitSupport.tryConsume());
        }

        boolean consumed = rateLimitSupport.consumeBlocking(Duration.ofSeconds(2));

        assertAll(
                () -> assertThat(consumedResults).containsOnly(true),
                () -> assertThat(consumed).isTrue(),
                () -> assertThat(rateLimitSupport.getLeasedTokens()).isEqualTo(4)
        );
    }

    @Test
    @DisplayName("초 경계가 지나면 남아 있던 임대 토큰은 폐기되고 새 임대를 받아 소비한다.")
    void expiresRemainingLeaseAtWallClockBoundary() {
        waitUntilNextSecondBoundary();

        DistributedRateLimitSupport rateLimitSupport = createRateLimitSupport();

        boolean consumedBeforeBoundary = rateLimitSupport.tryConsume();
        long leasedTokensBeforeBoundary = rateLimitSupport.getLeasedTokens();

        waitUntilNextSecondBoundary();

        boolean consumedAfterBoundary = rateLimitSupport.tryConsume();
        long leasedTokensAfterBoundary = rateLimitSupport.getLeasedTokens();

        assertAll(
                () -> assertThat(consumedBeforeBoundary).isTrue(),
                () -> assertThat(leasedTokensBeforeBoundary).isEqualTo(4),
                () -> assertThat(consumedAfterBoundary).isTrue(),
                () -> assertThat(leasedTokensAfterBoundary).isEqualTo(4)
        );
    }

    @Disabled
    @Test
    @DisplayName("서로 다른 임대량을 가진 두 클라이언트가 동시에 요청해도 전체 평균 처리량은 50 TPS를 넘지 않는다.")
    void keepsGlobalThroughputUnderFiftyAcrossClientsWithDifferentLeaseAmounts() throws Exception {
        waitUntilNextSecondBoundary();

        DistributedTokenLeaseService warmupLeaseService = new DistributedTokenLeaseService(
                dataSource,
                testBucketKey,
                capacity,
                capacity,
                refillSeconds
        );
        assertThat(warmupLeaseService.tryLeaseToken(capacity)).isTrue();

        DistributedRateLimitSupport apiRateLimitSupport = createRateLimitSupport(1, capacity);
        DistributedRateLimitSupport batchRateLimitSupport = createRateLimitSupport(5, capacity);

        Duration measurementWindow = Duration.ofSeconds(5);
        long startTimeNanos = System.nanoTime();
        long deadlineNanos = startTimeNanos + measurementWindow.toNanos();

        LoadTestResult result = runConcurrentLoad(apiRateLimitSupport, batchRateLimitSupport, deadlineNanos);

        double elapsedSeconds = result.elapsedNanos() / 1_000_000_000d;
        double apiTps = result.apiSuccessCount() / elapsedSeconds;
        double batchTps = result.batchSuccessCount() / elapsedSeconds;
        double totalTps = result.totalSuccessCount() / elapsedSeconds;

        System.out.printf(
                "apiSuccessCount=%d, batchSuccessCount=%d, totalSuccessCount=%d, elapsedSeconds=%.3f, apiTps=%.2f, batchTps=%.2f, totalTps=%.2f%n",
                result.apiSuccessCount(),
                result.batchSuccessCount(),
                result.totalSuccessCount(),
                elapsedSeconds,
                apiTps,
                batchTps,
                totalTps
        );

        assertAll(
                () -> assertThat(result.totalSuccessCount()).isPositive(),
                () -> assertThat(totalTps).isBetween(49.0, 50.5)
        );
    }

    @Disabled
    @Test
    @DisplayName("50 TPS 수준으로 토큰을 소비하고 정상적으로 갱신한다.")
    void maintainsConfiguredTpsAtFiftyAndRenewsLease() {
        waitUntilNextSecondBoundary();

        int targetPermitsPerSecond = 50;
        int targetLeaseAmount = 5;
        int measurementWindowSeconds = 5;
        Duration blockingTimeout = Duration.ofSeconds(2);
        long measurementWindowNanos = Duration.ofSeconds(measurementWindowSeconds).toNanos();
        AtomicInteger successfulLeaseCount = new AtomicInteger();

        DistributedRateLimitSupport rateLimitSupport = createTrackingRateLimitSupport(targetPermitsPerSecond, successfulLeaseCount, targetLeaseAmount);

        long startTimeNanos = System.nanoTime();
        long deadlineNanos = startTimeNanos + measurementWindowNanos;
        int successCount = 0;

        while (deadlineNanos - System.nanoTime() > 0) {
            if (rateLimitSupport.consumeBlocking(blockingTimeout)) {
                successCount++;
            }
        }

        long elapsedNanos = System.nanoTime() - startTimeNanos;

        double elapsedSeconds = elapsedNanos / 1_000_000_000d;
        double actualTps = successCount / elapsedSeconds;
        int expectedMinimumLeaseCount = (int) Math.ceil(successCount / (double) targetLeaseAmount);
        int finalSuccessCount = successCount;

        System.out.printf(
                "Configured TPS=%d, successCount=%d, measured TPS=%.2f, elapsedSeconds=%.3f, successfulLeaseCount=%d%n",
                targetPermitsPerSecond,
                finalSuccessCount,
                actualTps,
                elapsedSeconds,
                successfulLeaseCount.get()
        );

        assertAll(
                () -> assertThat(finalSuccessCount).isPositive(),
                () -> assertThat(actualTps).isBetween(targetPermitsPerSecond - 1.0, targetPermitsPerSecond + 1.0),
                () -> assertThat(successfulLeaseCount.get()).isGreaterThanOrEqualTo(expectedMinimumLeaseCount)
        );
    }

    private DistributedRateLimitSupport createTrackingRateLimitSupport(int targetPermitsPerSecond, AtomicInteger successfulLeaseCount, int targetLeaseAmount) {
        DistributedTokenLeaseService trackingLeaseService = new DistributedTokenLeaseService(
                dataSource,
                bucketKey + "-manual-50-tps",
                targetPermitsPerSecond,
                targetPermitsPerSecond,
                1
        ) {
            @Override
            public boolean tryLeaseToken(int leaseAmount) {
                boolean consumed = super.tryLeaseToken(leaseAmount);
                if (consumed) {
                    successfulLeaseCount.incrementAndGet();
                }
                return consumed;
            }
        };

        return new DistributedRateLimitSupport(
                trackingLeaseService,
                targetLeaseAmount
        );
    }

    private DistributedRateLimitSupport createRateLimitSupport() {
        return createRateLimitSupport(5, capacity);
    }

    private DistributedRateLimitSupport createRateLimitSupport(int leaseAmount, int permitsPerSecond) {
        DistributedTokenLeaseService isolatedLeaseService = new DistributedTokenLeaseService(
                dataSource,
                testBucketKey,
                permitsPerSecond,
                permitsPerSecond,
                refillSeconds
        );

        return new DistributedRateLimitSupport(isolatedLeaseService, leaseAmount);
    }

    private LoadTestResult runConcurrentLoad(
            DistributedRateLimitSupport apiRateLimitSupport,
            DistributedRateLimitSupport batchRateLimitSupport,
            long deadlineNanos
    ) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {
            Future<Integer> apiFuture = executorService.submit(() -> consumeUntilDeadline(apiRateLimitSupport, startLatch, deadlineNanos));
            Future<Integer> batchFuture = executorService.submit(() -> consumeUntilDeadline(batchRateLimitSupport, startLatch, deadlineNanos));

            long startedAtNanos = System.nanoTime();
            startLatch.countDown();

            int apiSuccessCount = apiFuture.get();
            int batchSuccessCount = batchFuture.get();
            long elapsedNanos = System.nanoTime() - startedAtNanos;

            return new LoadTestResult(apiSuccessCount, batchSuccessCount, elapsedNanos);
        } finally {
            executorService.shutdownNow();
        }
    }

    private int consumeUntilDeadline(
            DistributedRateLimitSupport rateLimitSupport,
            CountDownLatch startLatch,
            long deadlineNanos
    ) {
        try {
            startLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }

        int successCount = 0;
        while (deadlineNanos - System.nanoTime() > 0) {
            if (rateLimitSupport.tryConsume()) {
                successCount++;
                continue;
            }

            LockSupport.parkNanos(Duration.ofMillis(1).toNanos());
            if (Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return successCount;
    }

    private void waitUntilNextSecondBoundary() {
        long millisToNextSecondBoundary = 1_000 - (System.currentTimeMillis() % 1_000);
        sleep(millisToNextSecondBoundary + 100);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    private record LoadTestResult(int apiSuccessCount, int batchSuccessCount, long elapsedNanos) {

        private int totalSuccessCount() {
            return apiSuccessCount + batchSuccessCount;
        }
    }
}
