package maeilmail.admin;

import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;

record AdminQuestionRequest(
        Long id,
        String title,
        String content,
        String customizedTitle,
        String category
) {

    public Question toQuestion() {
        String actualCustomizedTitle = customizedTitle;
        if (customizedTitle == null || customizedTitle.isBlank()) {
            actualCustomizedTitle = null;
        }

        return new Question(id, title, content, actualCustomizedTitle, QuestionCategory.from(category));
    }

    public boolean isUpdate() {
        return id != null;
    }
}
