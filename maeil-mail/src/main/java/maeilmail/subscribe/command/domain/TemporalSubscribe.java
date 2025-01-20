package maeilmail.subscribe.command.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import maeilsupport.BaseEntity;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemporalSubscribe extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String verifyCode;

    @Column(nullable = false)
    private boolean isVerified;

    public TemporalSubscribe(String email, String verifyCode) {
        this(null, email, verifyCode, false);
    }

    public void verify(String code) {
        if (!verifyCode.equals(code)) {
            throw new IllegalArgumentException("올바른 인증 코드를 입력해주세요.");
        }

        this.isVerified = true;
    }
}
