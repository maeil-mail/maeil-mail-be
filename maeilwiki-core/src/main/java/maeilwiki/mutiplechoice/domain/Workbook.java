package maeilwiki.mutiplechoice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import maeilwiki.BaseEntity;
import maeilwiki.member.domain.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "multiple_choice_workbook")
public class Workbook extends BaseEntity {

    private static final int DEFAULT_SOLVED_COUNT = 0;
    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    @Column(nullable = false, columnDefinition = "INT")
    private int difficultyLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    private WorkbookCategory category;

    @Column(columnDefinition = "TEXT")
    private String workbookDetail;

    @Embedded
    private TimeLimit timeLimit;

    @Column(nullable = false, columnDefinition = "INT")
    private int solvedCount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Member member;

    public Workbook(
            String title,
            int difficultyLevel,
            String category,
            String workbookDetail,
            Integer timeLimit,
            Member member
    ) {
        validateTitle(title);
        validateDifficultyLevel(difficultyLevel);
        this.title = title;
        this.difficultyLevel = difficultyLevel;
        this.category = WorkbookCategory.from(category);
        this.workbookDetail = workbookDetail;
        this.timeLimit = new TimeLimit(timeLimit);
        this.solvedCount = DEFAULT_SOLVED_COUNT;
        this.member = member;
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("객관식 문제집의 제목은 필수 입력값입니다.");
        }

        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("객관식 문제집의 제목은 %d자 이하여야 합니다.".formatted(MAX_TITLE_LENGTH));
        }
    }

    private void validateDifficultyLevel(int difficultyLevel) {
        if (difficultyLevel < MIN_LEVEL || difficultyLevel > MAX_LEVEL) {
            String message = "객관식 문제집의 난이도는 %d ~ %d 사이의 값으로 설정해주세요.".formatted(MIN_LEVEL, MAX_LEVEL);
            throw new IllegalArgumentException(message);
        }
    }

    public void solve() {
        this.solvedCount++;
    }
}
