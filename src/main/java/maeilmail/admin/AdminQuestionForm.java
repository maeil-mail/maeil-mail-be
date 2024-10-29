package maeilmail.admin;

import lombok.Getter;
import lombok.Setter;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;

@Getter
@Setter
public class AdminQuestionForm {

    private Long id;
    private String title;
    private String content;
    private String category;

    public Question toQuestion() {
        return new Question(id, title, content, QuestionCategory.from(category));
    }

    public boolean isUpdate() {
        return id != null;
    }
}