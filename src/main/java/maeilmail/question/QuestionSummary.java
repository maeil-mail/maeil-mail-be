package maeilmail.question;

import com.querydsl.core.annotations.QueryProjection;

public record QuestionSummary(Long id, String title, String content, String category) {

    public QuestionSummary(Question question) {
        this(question.getId(), question.getTitle(), question.getContent(), question.getCategory().toLowerCase());
    }

    @QueryProjection
    public QuestionSummary {
    }
}
