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
public class MultipleChoiceOption extends BaseEntity {

    private static final int MAX_CONTENT_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = MAX_CONTENT_LENGTH)
    private String content;

    @Column(nullable = false)
    private boolean isCorrectAnswer;

    public MultipleChoiceOption(String content, boolean isCorrectAnswer) {
        validateContent(content);

        this.content = content;
        this.isCorrectAnswer = isCorrectAnswer;
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("객관식 항목의 내용은 필수 입력값입니다.");
        }

        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("객관식 항목의 내용은 %d자 이하여야 합니다.".formatted(MAX_CONTENT_LENGTH));
        }
    }
}
