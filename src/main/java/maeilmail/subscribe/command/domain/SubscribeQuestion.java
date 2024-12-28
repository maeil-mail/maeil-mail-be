package maeilmail.subscribe.command.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import maeilmail.question.Question;
import maeilmail.support.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "subscribe_question")
@Entity
public class SubscribeQuestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Subscribe subscribe;

    @ManyToOne(fetch = FetchType.LAZY)
    private Question question;

    private boolean isSuccess;

    public SubscribeQuestion(Subscribe subscribe, Question question, boolean isSuccess) {
        this.subscribe = subscribe;
        this.question = question;
        this.isSuccess = isSuccess;
    }

    public static SubscribeQuestion success(Subscribe subscribe, Question question) {
        return new SubscribeQuestion(subscribe, question, true);
    }

    public static SubscribeQuestion fail(Subscribe subscribe, Question question) {
        return new SubscribeQuestion(subscribe, question, false);
    }
}
