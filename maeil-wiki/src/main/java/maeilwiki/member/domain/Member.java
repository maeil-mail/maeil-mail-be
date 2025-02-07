package maeilwiki.member.domain;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import maeilsupport.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    private static final int MAX_NAME_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Column(nullable = false, unique = true)
    private String providerId;

    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    @Enumerated(value = EnumType.STRING)
    private Provider provider;

    @Column(nullable = true)
    private String githubUrl;

    @Setter
    @Column(nullable = false, unique = true)
    private String refreshToken;

    @Column(nullable = true)
    private String profileImageUrl;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

    public Member(String name, String providerId, String provider) {
        this(name, providerId, Provider.from(provider), null);
    }

    public Member(String name, String providerId, Provider provider, String profileImageUrl) {
        validateName(name);
        this.name = name;
        this.providerId = providerId;
        this.provider = provider;
        this.profileImageUrl = profileImageUrl;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수 입력값입니다.");
        }

        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("이름은 %d자 이하여야 합니다.".formatted(MAX_NAME_LENGTH));
        }
    }
}
