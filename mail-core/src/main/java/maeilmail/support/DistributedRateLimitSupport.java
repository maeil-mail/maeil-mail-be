package maeilmail.support;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.LockSupport;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Component
class DistributedRateLimitSupport {

    private static final long BLOCKING_RETRY_INTERVAL_NANOS = Duration.ofMillis(10).toNanos();

    @Getter(AccessLevel.NONE)
    private final DistributedTokenLeaseService leaseService;

    private final int leaseAmount;

    private long leasedTokens = 0;

    private long leaseExpiresAtMillis = 0;

    public DistributedRateLimitSupport(
            DistributedTokenLeaseService leaseService,
            @Value("${mail.ses.rate-limit.lease-amount}") int leaseAmount
    ) {
        this.leaseService = leaseService;
        this.leaseAmount = leaseAmount;
    }

    public boolean consumeBlocking(Duration timeout) {
        long timeoutNanos = timeout.toNanos();
        if (timeoutNanos <= 0) {
            throw new RateLimitExceededException();
        }

        long deadlineNanos = System.nanoTime() + timeoutNanos;
        while (true) {
            if (tryConsume()) {
                return true;
            }

            long remainingNanos = deadlineNanos - System.nanoTime();
            if (remainingNanos <= 0) {
                throw new RateLimitExceededException();
            }

            LockSupport.parkNanos(Math.min(BLOCKING_RETRY_INTERVAL_NANOS, remainingNanos));
            if (Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
                throw new RateLimitExceededException();
            }
        }
    }

    public synchronized boolean tryConsume() {
        expireLeasedTokens(Instant.now());

        if (!ensureLeasedTokens()) {
            return false;
        }

        return consume();
    }

    private void expireLeasedTokens(Instant now) {
        if (leasedTokens > 0 && (now.toEpochMilli() >= leaseExpiresAtMillis)) {
            log.debug("만료된 임대 토큰을 폐기했습니다. expiredTokens={}", leasedTokens);
            leasedTokens = 0;
            leaseExpiresAtMillis = 0;
        }
    }

    private boolean ensureLeasedTokens() {
        if (leasedTokens > 0) {
            return true;
        }

        if (leaseService.tryLeaseToken(leaseAmount)) {
            addLeasedTokens(leaseAmount, Instant.now());
            return true;
        }

        log.debug("임대 토큰을 획득하지 못했습니다.");
        return false;
    }

    private void addLeasedTokens(int amount, Instant acquiredAt) {
        leasedTokens = amount;
        leaseExpiresAtMillis = calculateNextRefillBoundary(acquiredAt).toEpochMilli();
    }

    private Instant calculateNextRefillBoundary(Instant now) {
        return Instant.ofEpochSecond(now.getEpochSecond() + 1);
    }

    private boolean consume() {
        if (leasedTokens < 1) {
            return false;
        }

        leasedTokens -= 1;
        if (leasedTokens == 0) {
            leaseExpiresAtMillis = 0;
        }

        return true;
    }
}
