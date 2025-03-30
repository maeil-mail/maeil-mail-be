package maeilwiki.mutiplechoice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import maeilsupport.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MultipleChoiceQuestion extends BaseEntity {

    private static final int MAX_TITLE_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String correctAnswerExplanation;

    public MultipleChoiceQuestion(String title, String correctAnswerExplanation) {
        validateTitle(title);
        this.title = title;
        this.correctAnswerExplanation = correctAnswerExplanation;
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("객관식 문제의 제목은 필수 입력값입니다.");
        }

        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("객관식 문제의 제목은 %d자 이하여야 합니다.".formatted(MAX_TITLE_LENGTH));
        }
    }
}
