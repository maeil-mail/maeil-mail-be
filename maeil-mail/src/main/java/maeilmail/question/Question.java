package maeilmail.question;

import jakarta.persistence.*;
import lombok.*;
import maeilsupport.BaseEntity;

@Entity
@Getter
@Setter
@Table(name = "question")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionCategory category;

    public Question(String title, String content, QuestionCategory category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }
}
