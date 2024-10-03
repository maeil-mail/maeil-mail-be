package maeilmail.question;

import com.querydsl.core.annotations.QueryProjection;

public record QuestionSummary(Long id, String title, String content, String category) {

    @QueryProjection
    public QuestionSummary {
    }
}
