package maeilbatch.forward;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import maeilmail.BaseEntity;
import maeilmail.mail.MailMessage;

@Getter
@Setter
@Entity
@Table(name = "forward_log")
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ForwardLog extends BaseEntity implements MailMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String target;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Setter
    @Enumerated(EnumType.STRING)
    private ForwardStatus status;

    public ForwardLog(String target, String subject, String message) {
        this.target = target;
        this.subject = subject;
        this.message = message;
        this.status = ForwardStatus.PENDING;
    }

    public boolean isRetryable() {
        return ForwardStatus.FAILED.equals(status) || ForwardStatus.PENDING.equals(status);
    }

    @Override
    public String getTo() {
        return target;
    }

    @Override
    public String getText() {
        return message;
    }
}
