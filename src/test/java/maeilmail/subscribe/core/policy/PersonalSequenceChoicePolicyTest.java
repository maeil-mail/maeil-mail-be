package maeilmail.subscribe.core.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionRepository;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.core.Subscribe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PersonalSequenceChoicePolicyTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private PersonalSequenceChoicePolicy personalSequenceChoicePolicy;

    @Test
    @DisplayName("구독일을 이용해 사용자별 순차 방식으로 질문지를 선택한다.")
    void choiceWithSubscribedAt() {
        LocalDateTime baseDateTime = LocalDateTime.of(2024, 10, 20, 0, 0);
        Subscribe subscribe = createSubscribe(QuestionCategory.BACKEND, baseDateTime);

        createQuestion("질문1", QuestionCategory.BACKEND);
        createQuestion("질문2", QuestionCategory.BACKEND);

        QuestionSummary choice = personalSequenceChoicePolicy.choice(subscribe, baseDateTime.toLocalDate());

        assertThat(choice.title()).isEqualTo("질문1");
    }

    @Test
    @DisplayName("구독일이 주어진 날짜보다 클 수 없다.")
    void cantChoice() {
        LocalDateTime baseDateTime = LocalDateTime.of(2024, 10, 20, 0, 0);
        LocalDate yesterday = baseDateTime.minusDays(1).toLocalDate();
        Subscribe subscribe = createSubscribe(QuestionCategory.BACKEND, baseDateTime);

        createQuestion("질문1", QuestionCategory.BACKEND);

        assertThatThrownBy(() -> personalSequenceChoicePolicy.choice(subscribe, yesterday))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("질문지를 결정할 수 없습니다.");
    }

    @Test
    @DisplayName("질문이 존재하지 않으면 질문지를 선택할 수 없다.")
    void cantChoice2() {
        LocalDateTime baseDateTime = LocalDateTime.of(2024, 10, 20, 0, 0);
        Subscribe subscribe = createSubscribe(QuestionCategory.BACKEND, baseDateTime);

        assertThatThrownBy(() -> personalSequenceChoicePolicy.choice(subscribe, baseDateTime.toLocalDate()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("질문지를 결정할 수 없습니다.");
    }

    /**
     * 24년 10월 21일부로 해당 기능이 적용되어야하는데, 10월 20일에 백엔드 5번 질문지를 발송됐다.
     * 24년 10월 20일자 기준, 기존 사용자들의 구독일이 null이다.
     * Period(2024-10-16, 2024-10-21).getDays() + 1 = 6번 질문지를 발송할 수 있으므로,
     * 백엔드의 구독일이 존재하지 않는 경우, 구독일을 10월 16일로 판단하여 기존 사용자가 받던 순서대로 질문을 선택한다.
     */
    @Test
    @DisplayName("백엔드 구독자가 구독일이 존재하지 않는 경우, 구독일을 10월 16일로 판단한다.")
    void backendDefaultChoice() {
        Subscribe subscribe = createSubscribe(QuestionCategory.BACKEND, null);
        createQuestions(10, QuestionCategory.BACKEND);

        QuestionSummary choice1 = personalSequenceChoicePolicy.choice(subscribe, LocalDate.of(2024, 10, 20));
        QuestionSummary choice2 = personalSequenceChoicePolicy.choice(subscribe, LocalDate.of(2024, 10, 21));
        QuestionSummary choice3 = personalSequenceChoicePolicy.choice(subscribe, LocalDate.of(2024, 10, 22));

        assertThat(choice1.title()).isEqualTo("질문5");
        assertThat(choice2.title()).isEqualTo("질문6");
        assertThat(choice3.title()).isEqualTo("질문7");
    }

    /**
     * 24년 10월 21일부로 해당 기능이 적용되어야하는데, 10월 20일에 프론트엔드 7번 질문지를 발송됐다.
     * 24년 10월 20일자 기준, 기존 사용자들의 구독일이 null이다.
     * Period(2024-10-14, 2024-10-21).getDays() + 1 = 8번 질문지를 발송할 수 있으므로,
     * 프론트엔드의 구독일이 존재하지 않는 경우, 구독일을 10월 14일로 판단하여 기존 사용자가 받던 순서대로 질문을 선택한다.
     */
    @Test
    @DisplayName("프론트엔드 구독자가 구독일이 존재하지 않는 경우, 구독일을 10월 14일로 판단한다.")
    void frontendDefaultChoice() {
        Subscribe subscribe = createSubscribe(QuestionCategory.FRONTEND, null);
        createQuestions(10, QuestionCategory.FRONTEND);

        QuestionSummary choice1 = personalSequenceChoicePolicy.choice(subscribe, LocalDate.of(2024, 10, 20));
        QuestionSummary choice2 = personalSequenceChoicePolicy.choice(subscribe, LocalDate.of(2024, 10, 21));
        QuestionSummary choice3 = personalSequenceChoicePolicy.choice(subscribe, LocalDate.of(2024, 10, 22));

        assertThat(choice1.title()).isEqualTo("질문7");
        assertThat(choice2.title()).isEqualTo("질문8");
        assertThat(choice3.title()).isEqualTo("질문9");
    }

    @Test
    @DisplayName("질문지를 결정한다.")
    void choice() {
        LocalDateTime baseDateTime = LocalDateTime.of(2024, 10, 20, 0, 0);
        Subscribe subscribe = createSubscribe(QuestionCategory.BACKEND, baseDateTime);

        createQuestion("질문1", QuestionCategory.BACKEND);
        createQuestion("질문2", QuestionCategory.BACKEND);
        createQuestion("질문3", QuestionCategory.BACKEND);
        createQuestion("질문4", QuestionCategory.FRONTEND);
        createQuestion("질문5", QuestionCategory.BACKEND);
        createQuestion("질문6", QuestionCategory.BACKEND);

        QuestionSummary choice1 = personalSequenceChoicePolicy.choice(subscribe, baseDateTime.toLocalDate());
        QuestionSummary choice2 = personalSequenceChoicePolicy.choice(subscribe, baseDateTime.toLocalDate().plusDays(1));
        QuestionSummary choice3 = personalSequenceChoicePolicy.choice(subscribe, baseDateTime.toLocalDate().plusDays(2));
        QuestionSummary choice4 = personalSequenceChoicePolicy.choice(subscribe, baseDateTime.toLocalDate().plusDays(3));
        QuestionSummary choice5 = personalSequenceChoicePolicy.choice(subscribe, baseDateTime.toLocalDate().plusDays(4));
        QuestionSummary choice6 = personalSequenceChoicePolicy.choice(subscribe, baseDateTime.toLocalDate().plusDays(5));

        assertThat(choice1.title()).isEqualTo("질문1");
        assertThat(choice2.title()).isEqualTo("질문2");
        assertThat(choice3.title()).isEqualTo("질문3");
        assertThat(choice4.title()).isEqualTo("질문5");
        assertThat(choice5.title()).isEqualTo("질문6");
        assertThat(choice6.title()).isEqualTo("질문1");
    }

    private void createQuestions(int size, QuestionCategory questionCategory) {
        for (int i = 1; i <= size; i++) {
            createQuestion("질문" + i, questionCategory);
        }
    }

    private void createQuestion(String questionTitle, QuestionCategory category) {
        Question question = new Question(questionTitle, "content", category);

        questionRepository.save(question);
    }

    private Subscribe createSubscribe(QuestionCategory category, LocalDateTime subscribedAt) {
        Subscribe subscribe = mock(Subscribe.class);
        when(subscribe.getCategory())
                .thenReturn(category);
        when(subscribe.getSubscribedAt())
                .thenReturn(subscribedAt);

        return subscribe;
    }
}
