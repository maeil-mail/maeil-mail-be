package maeilmail.subscribe.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class TemporalSubscribe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String verifyCode;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean isVerified;

    public TemporalSubscribe(String email, CodeGenerator codeGenerator) {
        this(null, codeGenerator.generateCode(), email, false);
    }

    public void verify(String code) {
        if (!verifyCode.equals(code)) {
            throw new IllegalArgumentException("인증되지 않은 이메일입니다.");
        }

        this.isVerified = true;
    }
}
