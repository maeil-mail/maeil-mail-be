package maeilmail.bulksend.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionRepository;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PersonalSequenceChoicePolicyTest extends IntegrationTestSupport {

    @Autowired
    private PersonalSequenceChoicePolicy policy;

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    @DisplayName("질문지가 존재하지 않으면 질문지를 선택할 수 없다.")
    void emptyQuestions() {
        Subscribe subscribe = createSubscribe(0L, QuestionCategory.FRONTEND);

        assertThatThrownBy(() -> policy.choice(subscribe))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("질문지를 결정할 수 없습니다.");
    }

    @Test
    @DisplayName("구독자의 시퀀스와 카테고리를 기반으로 질문지를 선택한다.")
    void choice() {
        Subscribe subscribe = createSubscribe(0L, QuestionCategory.FRONTEND);
        createQuestion("title1", QuestionCategory.BACKEND);
        createQuestion("title2", QuestionCategory.FRONTEND);
        createQuestion("title3", QuestionCategory.BACKEND);

        QuestionSummary result = policy.choice(subscribe);

        assertThat(result.title()).isEqualTo("title2");
    }

    @Test
    @DisplayName("구독자의 시퀀스가 전체 질문지의 범위를 벗어나면 처음부터 순회한다.")
    void rotation() {
        Subscribe subscribe = createSubscribe(3L, QuestionCategory.FRONTEND);
        createQuestion("title1", QuestionCategory.FRONTEND);
        createQuestion("title2", QuestionCategory.FRONTEND);
        createQuestion("title3", QuestionCategory.FRONTEND);
        createQuestion("title4", QuestionCategory.BACKEND);

        QuestionSummary result = policy.choice(subscribe);

        assertThat(result.title()).isEqualTo("title1");
    }

    @Test
    @DisplayName("특정 회차를 감안하여 질문지를 선택한다.")
    void choiceByRound() {
        Subscribe subscribe = createSubscribe(0L, QuestionCategory.FRONTEND);
        createQuestion("title1", QuestionCategory.FRONTEND);
        createQuestion("title2", QuestionCategory.FRONTEND);
        createQuestion("title3", QuestionCategory.FRONTEND);

        QuestionSummary result = policy.choiceByRound(subscribe, 3);

        assertThat(result.title()).isEqualTo("title1");
    }

    private Subscribe createSubscribe(Long sequence, QuestionCategory category) {
        Subscribe subscribe = mock(Subscribe.class);
        when(subscribe.getNextQuestionSequence())
                .thenReturn(sequence);
        when((subscribe.getCategory()))
                .thenReturn(category);

        return subscribe;
    }

    private Question createQuestion(String title, QuestionCategory category) {
        Question question = new Question(title, "test", category);

        return questionRepository.save(question);
    }
}
