package maeilbatch;

import jakarta.mail.Transport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class SmtpConnectionPool implements AutoCloseable {

    private final GenericObjectPool<Transport> connectionPool;

    @Override
    public void close() {
        connectionPool.close();
    }

    public SmtpConnectionPool(SmtpConnectionProperties settings) {
        GenericObjectPoolConfig<Transport> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(8);
        config.setMaxIdle(8);
        config.setMinIdle(0);
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(true);
        config.setBlockWhenExhausted(true);

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
