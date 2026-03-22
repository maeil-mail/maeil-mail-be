package maeilmail.support;

import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.DateTimeProvider;

public class TestAuditingSupport {

    private final AuditingHandler auditingHandler;
    private final DateTimeProvider dateTimeProvider;

    public TestAuditingSupport(AuditingHandler auditingHandler, DateTimeProvider dateTimeProvider) {
        this.auditingHandler = auditingHandler;
        this.dateTimeProvider = dateTimeProvider;
    }

    public void setJpaAuditingTime(LocalDateTime time) {
        when(dateTimeProvider.getNow())
                .thenReturn(Optional.of(time));
        auditingHandler.setDateTimeProvider(dateTimeProvider);
    }
}
