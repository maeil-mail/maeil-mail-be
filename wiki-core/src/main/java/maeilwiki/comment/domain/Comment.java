package maeilwiki.comment.domain;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import maeilwiki.BaseEntity;
import maeilwiki.member.domain.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(nullable = false)
    private boolean isAnonymous;

    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Member member;

    @Column(nullable = false)
    private Long wikiId;

    public Comment(String answer, boolean isAnonymous, Member member, Long wikiId) {
        validateAnswer(answer);
        this.answer = answer;
        this.isAnonymous = isAnonymous;
        this.member = member;
        this.wikiId = wikiId;
    }

    private void validateAnswer(String answer) {
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("답변은 필수 입력값입니다.");
        }
    }

    public void remove() {
        if (deletedAt != null) {
            throw new IllegalStateException("이미 삭제된 답변입니다.");
        }

        deletedAt = LocalDateTime.now();
    }
}
