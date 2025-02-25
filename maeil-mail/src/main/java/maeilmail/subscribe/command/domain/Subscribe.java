package maeilmail.subscribe.command.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import maeilmail.question.QuestionCategory;
import maeilsupport.BaseEntity;

@Entity
@Getter
@Table(name = "subscribe")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscribe extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionCategory category;

    @Column(nullable = false)
    private Long nextQuestionSequence;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscribeFrequency frequency;

    public Subscribe(String email, QuestionCategory category, SubscribeFrequency frequency) {
        this.email = email;
        this.category = category;
        this.nextQuestionSequence = determineSequenceByCategory(category);
        this.token = UUID.randomUUID().toString();
        this.frequency = frequency;
        this.deletedAt = null;
    }

    private long determineSequenceByCategory(QuestionCategory category) {
        if (category == QuestionCategory.BACKEND) {
            return 15L;
        }

        return 0L;
    }

    public void unsubscribe() {
        if (deletedAt != null) {
            throw new IllegalStateException("이미 구독이 취소되었습니다.");
        }

        this.deletedAt = LocalDateTime.now();
    }

    public void changeFrequency(SubscribeFrequency frequency) {
        this.frequency = frequency;
    }

    public boolean isMine(String token) {
        return this.token.equals(token);
    }
}
