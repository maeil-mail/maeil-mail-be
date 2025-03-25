package maeilwiki.wiki.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import maeilsupport.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MultipleChoiceQuestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "detail", nullable = false)
    private String content;

    @Column(name = "isAnswer", nullable = false)
    private boolean isAnswer;

    @ManyToOne
    @JoinColumn(name = "multiple_choice_wiki_id")
    private MultipleChoiceWiki multipleChoiceWiki;

    public MultipleChoiceQuestion(String content, boolean isAnswer) {
        this.content = content;
        this.isAnswer = isAnswer;
    }

    public MultipleChoiceQuestion applyMultipleChoiceWiki(MultipleChoiceWiki multipleChoiceWiki) {
        this.multipleChoiceWiki = multipleChoiceWiki;
        return this;
    }
}
