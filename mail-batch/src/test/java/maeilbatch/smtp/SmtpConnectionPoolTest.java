package maeilbatch.smtp;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SmtpConnectionPoolTest {

    @Test
    @DisplayName("콜백이 성공하면 커넥션을 풀에 반납한다.")
    void doWithConnectionSuccess() throws Exception {
        GenericObjectPool<Transport> delegatePool = mock(GenericObjectPool.class);
        Transport transport = mock(Transport.class);
        SmtpConnectionCallback callback = mock(SmtpConnectionCallback.class);

        when(delegatePool.borrowObject()).thenReturn(transport);

        SmtpConnectionPool pool = createPool(delegatePool);

        pool.doWithConnection(callback);

        assertAll(
                () -> verify(delegatePool, times(1)).borrowObject(),
                () -> verify(callback, times(1)).execute(transport),
                () -> verify(delegatePool, times(1)).returnObject(transport),
                () -> verify(delegatePool, never()).invalidateObject(transport)
        );
    }

    @Test
    @DisplayName("콜백이 실패하면 커넥션을 invalidate 한다.")
    void doWithConnectionFailure() throws Exception {
        GenericObjectPool<Transport> delegatePool = mock(GenericObjectPool.class);
        Transport transport = mock(Transport.class);
        SmtpConnectionCallback callback = mock(SmtpConnectionCallback.class);

        when(delegatePool.borrowObject()).thenReturn(transport);
        doThrow(new IllegalStateException("send fail"))
                .when(callback)
                .execute(transport);

        SmtpConnectionPool pool = createPool(delegatePool);

        assertThatThrownBy(() -> pool.doWithConnection(callback))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("send fail");

        assertAll(
                () -> verify(delegatePool, times(1)).borrowObject(),
                () -> verify(callback, times(1)).execute(transport),
                () -> verify(delegatePool, times(1)).invalidateObject(transport),
                () -> verify(delegatePool, never()).returnObject(transport)
        );
    }

    @Test
    @DisplayName("close 호출 시 내부 GenericObjectPool을 닫는다.")
    void closePool() {
        GenericObjectPool<Transport> delegatePool = mock(GenericObjectPool.class);
        SmtpConnectionPool pool = createPool(delegatePool);

        pool.close();

        verify(delegatePool, times(1)).close();
    }

    private SmtpConnectionPool createPool(GenericObjectPool<Transport> delegatePool) {
        Session session = Session.getInstance(new Properties());
        SmtpConnectionProperties settings = new SmtpConnectionProperties(
                session,
                "localhost",
                25,
                "smtp",
                null,
                null
        );
        SmtpConnectionPool pool = new SmtpConnectionPool(settings);
        ReflectionTestUtils.setField(pool, "connectionPool", delegatePool);
        return pool;
    }
}
