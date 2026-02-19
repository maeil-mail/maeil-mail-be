package maeilmail.question;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private QuestionCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_question_id", nullable = false)
    private Question startQuestion;

    public CategoryPolicy(QuestionCategory category, Question startQuestion) {
        this.category = category;
        this.startQuestion = startQuestion;
    }

    public void updateStartQuestion(Question startQuestion) {
        this.startQuestion = startQuestion;
    }
}
