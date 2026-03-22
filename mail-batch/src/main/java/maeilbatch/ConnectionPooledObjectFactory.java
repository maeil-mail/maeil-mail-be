package maeilbatch;

import jakarta.mail.Session;
import jakarta.mail.Transport;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.springframework.mail.javamail.JavaMailSenderImpl;

class ConnectionPooledObjectFactory extends BasePooledObjectFactory<Transport> {

    private final JavaMailSenderImpl delegate;

    public ConnectionPooledObjectFactory(JavaMailSenderImpl delegate) {
        this.delegate = delegate;
    }

    @Override
    public Transport create() throws Exception {
        String username = emptyToNull(delegate.getUsername());
        String password = emptyToNull(delegate.getPassword());
        Session session = delegate.getSession();
        String protocol = delegate.getProtocol() != null ? delegate.getProtocol() : session.getProperty("mail.transport.protocol");
        Transport transport = session.getTransport(protocol != null ? protocol : JavaMailSenderImpl.DEFAULT_PROTOCOL);
        transport.connect(delegate.getHost(), delegate.getPort(), username, password);

        return transport;
    }

    @Override
    public PooledObject<Transport> wrap(Transport transport) {
        return new DefaultPooledObject<>(transport);
    }

    @Override
    public boolean validateObject(PooledObject<Transport> pooledObject) {
        return pooledObject.getObject().isConnected();
    }

    @Override
    public void destroyObject(PooledObject<Transport> pooledObject) throws Exception {
        Transport transport = pooledObject.getObject();
        if (transport.isConnected()) {
            transport.close();
        }
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
