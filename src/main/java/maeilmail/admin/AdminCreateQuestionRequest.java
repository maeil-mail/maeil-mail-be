package maeilmail.admin;

import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;

record AdminCreateQuestionRequest(String title, String content, String category) {

    public Question toDomainEntity() {
        return new Question(title, content, QuestionCategory.from(category));
    }
}
