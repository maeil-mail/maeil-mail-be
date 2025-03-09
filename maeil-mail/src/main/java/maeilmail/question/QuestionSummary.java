package maeilmail.question;

import java.time.LocalDateTime;
import com.querydsl.core.annotations.QueryProjection;

public record QuestionSummary(
        Long id,
        String title,
        String content,
        String category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    @QueryProjection
    public QuestionSummary {
    }

    public Question toQuestion() {
        return new Question(this.id, this.title, this.content, QuestionCategory.from(this.category));
    }
}
