package maeilbatch.smtp;

import jakarta.mail.Session;
import jakarta.mail.Transport;
import lombok.RequiredArgsConstructor;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

@RequiredArgsConstructor
class SmtpConnectionFactory extends BasePooledObjectFactory<Transport> {

    private final SmtpConnectionProperties settings;

    @Override
    public Transport create() throws Exception {
        Session session = settings.session();
        Transport transport = session.getTransport(settings.protocol());
        transport.connect(settings.host(), settings.port(), settings.username(), settings.password());

        return transport;
    }

    @Override
    public PooledObject<Transport> wrap(Transport transport) {
        return new DefaultPooledObject<>(transport);
    }

    /**
     * stale connection case에서 SMTPTransport는 noop이나 rset을 보내서 검사한다.(네트워크 왕복 기반 체크)
     *
     * @see org.eclipse.angus.mail.smtp.SMTPTransport
     */
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
}
