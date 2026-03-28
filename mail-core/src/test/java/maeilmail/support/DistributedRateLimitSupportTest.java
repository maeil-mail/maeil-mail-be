package maeilmail.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DistributedRateLimitSupportTest {

    @Test
    @DisplayName("임대 토큰을 발급받지 못하면 토큰을 소비할 수 없다.")
    void cannotConsumeWhenLeaseCannotBeAcquired() {
        DistributedTokenLeaseService leaseService = mock(DistributedTokenLeaseService.class);
        when(leaseService.tryLeaseToken(5)).thenReturn(false);
        DistributedRateLimitSupport rateLimitSupport = createRateLimitSupport(leaseService);

        boolean consumed = rateLimitSupport.tryConsume();

        assertAll(
                () -> assertThat(consumed).isFalse(),
                () -> verify(leaseService).tryLeaseToken(5)
        );
    }

    @Test
    @DisplayName("임대 토큰을 획득하면 바로 토큰을 소비할 수 있다.")
    void consumesTokenWhenLeaseIsAcquired() {
        DistributedTokenLeaseService leaseService = mock(DistributedTokenLeaseService.class);
        when(leaseService.tryLeaseToken(5)).thenReturn(true);
        DistributedRateLimitSupport rateLimitSupport = createRateLimitSupport(leaseService);

        boolean consumed = rateLimitSupport.tryConsume();

        assertAll(
                () -> assertThat(consumed).isTrue(),
                () -> assertThat(rateLimitSupport.getLeasedTokens()).isEqualTo(4),
                () -> verify(leaseService).tryLeaseToken(5)
        );
    }

    @Test
    @DisplayName("임대 토큰이 존재하지 않을 때만, 임대 요청이 발생한다.")
    void requestsLeaseOnlyWhenNoLeasedTokensRemain() {
        DistributedTokenLeaseService leaseService = mock(DistributedTokenLeaseService.class);
        when(leaseService.tryLeaseToken(5)).thenReturn(true);
        DistributedRateLimitSupport rateLimitSupport = createRateLimitSupport(leaseService);

        rateLimitSupport.tryConsume();
        setLeaseExpiresAtMillis(rateLimitSupport, Long.MAX_VALUE);
        rateLimitSupport.tryConsume();

        verify(leaseService, times(1)).tryLeaseToken(5);
    }

    @Test
    @DisplayName("한 번 임대 받으면 최대 임대량만큼은 추가 임대 없이 소비한다.")
    void consumesUpToLeaseAmountWithoutAdditionalLease() {
        DistributedTokenLeaseService leaseService = mock(DistributedTokenLeaseService.class);
        when(leaseService.tryLeaseToken(5)).thenReturn(true);
        DistributedRateLimitSupport rateLimitSupport = createRateLimitSupport(leaseService);
        List<Boolean> consumedResults = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            if (i == 1) {
                setLeaseExpiresAtMillis(rateLimitSupport, Long.MAX_VALUE);
            }

            consumedResults.add(rateLimitSupport.tryConsume());
        }

        assertAll(
                () -> assertThat(consumedResults).containsOnly(true),
                () -> verify(leaseService, times(1)).tryLeaseToken(5)
        );
    }

    @Test
    @DisplayName("임대 토큰량을 모두 사용하면 다시 임대 요청을 수행한다.")
    void requestsLeaseAgainAfterUsingAllLeasedTokens() {
        DistributedTokenLeaseService leaseService = mock(DistributedTokenLeaseService.class);
        when(leaseService.tryLeaseToken(5)).thenReturn(true);
        DistributedRateLimitSupport rateLimitSupport = createRateLimitSupport(leaseService);
        List<Boolean> consumedResults = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            consumedResults.add(rateLimitSupport.tryConsume());
            setLeaseExpiresAtMillis(rateLimitSupport, Long.MAX_VALUE);
        }

        consumedResults.add(rateLimitSupport.tryConsume());

        assertAll(
                () -> assertThat(consumedResults).containsOnly(true),
                () -> verify(leaseService, times(2)).tryLeaseToken(5)
        );
    }

    @Test
    @DisplayName("임대 기간이 지나지 않은 토큰은 사용할 수 있다.")
    void canUseLeasedTokenBeforeItExpires() {
        DistributedTokenLeaseService leaseService = mock(DistributedTokenLeaseService.class);
        DistributedRateLimitSupport rateLimitSupport = createRateLimitSupport(leaseService);
        setLeasedTokens(rateLimitSupport, 1);
        setLeaseExpiresAtMillis(rateLimitSupport, System.currentTimeMillis() + 1_000);

        boolean consumed = rateLimitSupport.tryConsume();

        assertAll(
                () -> assertThat(consumed).isTrue(),
                () -> assertThat(rateLimitSupport.getLeasedTokens()).isZero(),
                () -> verify(leaseService, never()).tryLeaseToken(5)
        );
    }

    @Test
    @DisplayName("임대 기한이 지난 토큰은 폐기된다.")
    void discardsExpiredLeasedTokensAndRequestsNewLease() {
        DistributedTokenLeaseService leaseService = mock(DistributedTokenLeaseService.class);
        when(leaseService.tryLeaseToken(5)).thenReturn(true);
        DistributedRateLimitSupport rateLimitSupport = createRateLimitSupport(leaseService);
        setLeasedTokens(rateLimitSupport, 3);
        setLeaseExpiresAtMillis(rateLimitSupport, System.currentTimeMillis() - 1);

        boolean consumed = rateLimitSupport.tryConsume();

        assertAll(
                () -> assertThat(consumed).isTrue(),
                () -> assertThat(rateLimitSupport.getLeasedTokens()).isEqualTo(4),
                () -> verify(leaseService).tryLeaseToken(5)
        );
    }

    @Test
    @DisplayName("임대 토큰을 timeout 안에 획득하지 못하면 예외를 발생한다.")
    void consumeBlockingThrowsExceptionWhenTimeoutExpires() {
        DistributedTokenLeaseService leaseService = mock(DistributedTokenLeaseService.class);
        when(leaseService.tryLeaseToken(5)).thenReturn(false);
        DistributedRateLimitSupport rateLimitSupport = createRateLimitSupport(leaseService);

        assertAll(
                () -> assertThatThrownBy(() -> rateLimitSupport.consumeBlocking(Duration.ofMillis(5)))
                        .isInstanceOf(RateLimitExceededException.class)
                        .hasMessage("처리율 제한을 초과했습니다."),
                () -> assertThat(rateLimitSupport.getLeasedTokens()).isZero(),
                () -> verify(leaseService, atLeastOnce()).tryLeaseToken(5)
        );
    }

    private DistributedRateLimitSupport createRateLimitSupport(DistributedTokenLeaseService leaseService) {
        return new DistributedRateLimitSupport(leaseService, 5);
    }

    private void setLeasedTokens(DistributedRateLimitSupport support, long leasedTokens) {
        setField(support, "leasedTokens", leasedTokens);
    }

    private void setLeaseExpiresAtMillis(DistributedRateLimitSupport support, long leaseExpiresAtMillis) {
        setField(support, "leaseExpiresAtMillis", leaseExpiresAtMillis);
    }

    private void setField(DistributedRateLimitSupport support, String fieldName, Object value) {
        try {
            Field field = DistributedRateLimitSupport.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(support, value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
