package maeilwiki.wiki.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import maeilsupport.BaseEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MultipleChoiceWiki extends BaseEntity {

    private static final int MAX_TITLE_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    @Column(name = "wiki_category", nullable = false, columnDefinition = "VARCHAR(10)")
    @Enumerated(value = EnumType.STRING)
    private WikiCategory category;

    @Column(name = "is_anonymous", nullable = false)
    private boolean isAnonymous;

    @Column(name = "difficulty_level", nullable = false)
    private Integer difficultyLevel;

    @OneToMany(mappedBy = "multipleChoiceWiki", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MultipleChoiceQuestion> multipleChoiceQuestions = new ArrayList<>();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    public MultipleChoiceWiki(String title, String category, boolean isAnonymous, int difficultyLevel, List<MultipleChoiceQuestion> multipleChoiceQuestions, Long memberId) {
        this(title, "", category, isAnonymous, difficultyLevel, multipleChoiceQuestions, memberId);
    }

    public MultipleChoiceWiki(String title, String detail, String category, boolean isAnonymous, int difficultyLevel, List<MultipleChoiceQuestion> multipleChoiceQuestions, Long memberId) {
        validateTitle(title);
        validateDifficultyLevel(difficultyLevel);
        validateMultipleChoiceQuestions(multipleChoiceQuestions);
        validateMember(memberId);
        this.title = title;
        this.detail = detail;
        this.category = WikiCategory.from(category);
        this.isAnonymous = isAnonymous;
        this.difficultyLevel = difficultyLevel;
        this.multipleChoiceQuestions = multipleChoiceQuestions.stream().map(it -> it.applyMultipleChoiceWiki(this)).toList();
        this.memberId = memberId;
    }

    private void validateMember(Long memberId) {
        if (memberId == null || memberId == 0) {
            throw new IllegalArgumentException("작성 회원은 비어있을 수 없습니다.");
        }
    }

    private void validateMultipleChoiceQuestions(List<MultipleChoiceQuestion> multipleChoiceQuestions) {
        if (multipleChoiceQuestions == null || multipleChoiceQuestions.isEmpty()) {
            throw new IllegalArgumentException("객관식 문제는 최소 1개 이상 존재해야 합니다.");
        }

        if (multipleChoiceQuestions.size() > 10) {
            throw new IllegalArgumentException("객관식 문제는 최대 10개까지 등록할 수 있습니다.");
        }
    }

    private void validateDifficultyLevel(int difficultyLevel) {
        if (difficultyLevel < 1 || difficultyLevel > 5) {
            throw new IllegalArgumentException("난이도는 1~5 범위 이내로 설정해야 합니다.");
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("질문은 필수 입력값입니다.");
        }

        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("질문은 %d자 이하여야 합니다.".formatted(MAX_TITLE_LENGTH));
        }
    }

    public void remove() {
        if (deletedAt != null) {
            throw new IllegalStateException("이미 삭제된 위키입니다.");
        }

        deletedAt = LocalDateTime.now();
    }
}
