package maeilwiki.wiki.domain;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import maeilsupport.BaseEntity;
import maeilwiki.member.domain.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wiki extends BaseEntity {

    private static final int MAX_QUESTION_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = MAX_QUESTION_LENGTH)
    private String question;

    @Column(length = MAX_QUESTION_LENGTH)
    private String questionDetail;

    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    @Enumerated(value = EnumType.STRING)
    private WikiCategory category;

    @Column(nullable = false)
    private boolean isAnonymous;

    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Member member;

    public Wiki(String question, String category, boolean isAnonymous, Member member) {
        this(question, null, category, isAnonymous, member);
    }

    public Wiki(String question, String questionDetail, String category, boolean isAnonymous, Member member) {
        validateQuestion(question);
        this.question = question;
        this.questionDetail = questionDetail;
        this.category = WikiCategory.from(category);
        this.isAnonymous = isAnonymous;
        this.member = member;
    }

    private void validateQuestion(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("질문은 필수 입력값입니다.");
        }

        if (question.length() > MAX_QUESTION_LENGTH) {
            throw new IllegalArgumentException("질문은 %d자 이하여야 합니다.".formatted(MAX_QUESTION_LENGTH));
        }
    }

    public void remove() {
        if (deletedAt != null) {
            throw new IllegalStateException("이미 삭제된 위키입니다.");
        }

        deletedAt = LocalDateTime.now();
    }
}
