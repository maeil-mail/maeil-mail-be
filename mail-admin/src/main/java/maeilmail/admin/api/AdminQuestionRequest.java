package maeilmail.admin.api;

import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;

record AdminQuestionRequest(
        Long id,
        String title,
        String content,
        String category
) {

    public Question toQuestion() {
        return new Question(id, title, content, QuestionCategory.from(category));
    }

    public boolean isUpdate() {
        return id != null;
    }
}
