package maeilmail.subscribe.core.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionRepository;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.core.Subscribe;
import maeilmail.subscribe.core.SubscribeRepository;
import maeilmail.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 9월 23일 기준으로 카테고리 별 첫 번째 질문지가 발송된다.
 */
class SequenceChoicePolicyTest extends IntegrationTestSupport {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private SequenceChoicePolicy sequenceChoicePolicy;

    @Test
    @DisplayName("24년 9월 23일보다 이전 값이 들어오면 질문지를 선택할 수 없다.")
    void cantChoice() {
        Subscribe subscribe = createBackendSubscribe();
        LocalDate today = LocalDate.of(2024, 9, 22);

        assertThatThrownBy(() -> sequenceChoicePolicy.choice(subscribe, today))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("질문지를 결정할 수 없습니다.");
    }

    @Test
    @DisplayName("질문이 존재하지 않으면 질문지를 선택할 수 없다.")
    void cantChoice2() {
        Subscribe subscribe = createBackendSubscribe();
        LocalDate today = LocalDate.of(2024, 9, 23);

        assertThatThrownBy(() -> sequenceChoicePolicy.choice(subscribe, today))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("질문지를 결정할 수 없습니다.");
    }

    @Test
    @DisplayName("질문지를 결정한다.")
    void choice() {
        Subscribe subscribe = createBackendSubscribe();
        createQuestion("질문1", QuestionCategory.BACKEND);
        createQuestion("질문2", QuestionCategory.BACKEND);
        createQuestion("질문3", QuestionCategory.BACKEND);
        createQuestion("질문4", QuestionCategory.FRONTEND);
        createQuestion("질문5", QuestionCategory.BACKEND);
        createQuestion("질문6", QuestionCategory.BACKEND);

        QuestionSummary choice1 = sequenceChoicePolicy.choice(subscribe, LocalDate.of(2024, 9, 23));
        QuestionSummary choice2 = sequenceChoicePolicy.choice(subscribe, LocalDate.of(2024, 9, 24));
        QuestionSummary choice3 = sequenceChoicePolicy.choice(subscribe, LocalDate.of(2024, 9, 25));
        QuestionSummary choice4 = sequenceChoicePolicy.choice(subscribe, LocalDate.of(2024, 9, 26));
        QuestionSummary choice5 = sequenceChoicePolicy.choice(subscribe, LocalDate.of(2024, 9, 27));

        assertThat(choice1.title()).isEqualTo("질문1");
        assertThat(choice2.title()).isEqualTo("질문2");
        assertThat(choice3.title()).isEqualTo("질문3");
        assertThat(choice4.title()).isEqualTo("질문5");
        assertThat(choice5.title()).isEqualTo("질문6");
    }

    private Question createQuestion(String questionTitle, QuestionCategory category) {
        Question question = new Question(questionTitle, "content", category);

        return questionRepository.save(question);
    }

    private Subscribe createBackendSubscribe() {
        Subscribe subscribe = new Subscribe("lee@gmail.com", QuestionCategory.BACKEND);

        return subscribeRepository.save(subscribe);
    }
}
