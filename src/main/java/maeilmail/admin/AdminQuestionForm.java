package maeilmail.admin;

import lombok.Getter;
import lombok.Setter;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;

@Getter
@Setter
public class AdminQuestionForm {

    private String title;
    private String content;
    private String category;

    public Question toQuestion() {
        return new Question(title, content, QuestionCategory.from(category));
    }
}
