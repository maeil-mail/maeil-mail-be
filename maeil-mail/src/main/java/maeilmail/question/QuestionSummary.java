package maeilmail.question;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record QuestionSummary(
        Long id,
        String title,
        String content,
        String customizedTitle,
        String category,
        LocalDateTime createdDateTime,
        LocalDateTime modifiedDateTime
) {

    @QueryProjection
    public QuestionSummary {
    }

    public Question toQuestion() {
        return new Question(this.id, this.title, this.content, this.customizedTitle, QuestionCategory.from(this.category));
    }
}
