package maeilbatch;

import java.time.Duration;
import jakarta.mail.Transport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class SmtpConnectionPool implements AutoCloseable {

    private final GenericObjectPool<Transport> connectionPool;

    @Override
    public void close() {
        connectionPool.close();
    }

    /**
     * maxTotal = 최대 pooled object의 수
     * maxIdle = 최대 idle object의 수(반납할 때 해당 수를 넘기면, 제거됨)
     * minIdle = 최소 idle object의 수(0으로 설정하면 lazy 실행됨)
     * setTestOnBorrow = 빌리는 시점에 idle 커넥션 검사 수행(factory.validateObject() 호출)
     * setTestOnIdle = evictor가 별도 스레드로 동작하면서 idle 커넥션을 검사함. -> GenericObjectPool.evict() 문서 참고
     * setMinEvictableIdleDuration = idle 커넥션 검사 시점에 성공했지만, 너무 오랜 기간 커넥션을 사용하는 경우 제거하는 옵션 -> GenericObjectPool.evict() 문서 참고
     *    - AWS SES SMTP 엔드포인트는 ELB 뒤에서 실행된다. 애플리케이션이 ELB를 통해서 EC2와 연결되기 때문에 EC2 인스턴스가 종료되면 연결이 무효화된다.
     *    - 단일 SMTP 연결을 통해 고정된 수의 메시지를 전송한 후, 또는 SMTP 연결이 일정 시간 활성 상태를 유지한 후 새 SMTP 연결을 설정하는 것을 권장한다.
     *    - 애플리케이션이 호스팅되는 위치와 이메일 제출 방식에 따라 임계값을 찾아야 한다.
     *    - 참고로, AWS SES는 비활성 커넥션이 10초 이상 지속되면 종료한다.
     * setTimeBetweenEvictionRuns = evictor 스레드 실행 간 대기 시간
     *
     */
    public SmtpConnectionPool(SmtpConnectionProperties settings) {
        GenericObjectPoolConfig<Transport> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(8);
        config.setMaxIdle(8);
        config.setMinIdle(0);
        config.setTestWhileIdle(true);
        config.setBlockWhenExhausted(true);
        config.setMinEvictableIdleDuration(Duration.ofSeconds(8));
        config.setTimeBetweenEvictionRuns(Duration.ofSeconds(5));
        config.setMaxWait(Duration.ofSeconds(10));

        this.connectionPool = new GenericObjectPool<>(new SmtpConnectionFactory(settings), config);
    }

    public void doWithConnection(TransportCallback cb) throws Exception {
        Transport transport = connectionPool.borrowObject();
        boolean invalidateTransport = false;

        try {
            cb.execute(transport);
        } catch (Exception e) {
            invalidateTransport = true;
            throw e;
        } finally {
            returnOrInvalidate(transport, invalidateTransport);
        }
    }

    private void returnOrInvalidate(Transport transport, boolean invalidateTransport) throws Exception {
        if (transport == null) {
            return;
        }

        if (invalidateTransport) {
            connectionPool.invalidateObject(transport);
            return;
        }

        connectionPool.returnObject(transport);
    }

    @FunctionalInterface
    public interface TransportCallback {

        void execute(Transport transport) throws Exception;
    }
}
