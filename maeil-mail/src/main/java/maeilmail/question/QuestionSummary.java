package maeilmail.question;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

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
