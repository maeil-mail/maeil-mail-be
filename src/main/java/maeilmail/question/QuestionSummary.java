package maeilmail.question;

import com.querydsl.core.annotations.QueryProjection;

public record QuestionSummary(Long id, String title, String content, String customizedTitle, String category) {

    @QueryProjection
    public QuestionSummary {
    }
}
