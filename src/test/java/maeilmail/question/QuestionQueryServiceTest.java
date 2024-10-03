package maeilmail.question;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class QuestionQueryServiceTest {

    @Autowired
    private QuestionQueryService questionQueryService;

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    @DisplayName("주어진 식별자에 해당하는 질문지를 조회한다.")
    void queryOneById() {
        Question question = createQuestion(QuestionCategory.BACKEND);

        QuestionSummary result = questionQueryService.queryOneById(question.getId());

        assertThat(result.id()).isEqualTo(question.getId());
        assertThat(result.category()).isEqualTo("backend");
    }

    @Test
    @DisplayName("주어진 식별자에 해당하는 질문지가 없다면 예외를 발생한다.")
    void notFound() {
        assertThatThrownBy(() -> questionQueryService.queryOneById(-1L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("주어진 카테고리에 해당하는 질문지를 조회한다.")
    void queryAllByCategory() {
        createQuestion(QuestionCategory.BACKEND);
        createQuestion(QuestionCategory.BACKEND);
        createQuestion(QuestionCategory.FRONTEND);

        List<QuestionSummary> backend = questionQueryService.queryAllByCategory("backend");
        List<QuestionSummary> frontend = questionQueryService.queryAllByCategory("frontend");

        assertThat(backend.size()).isEqualTo(2);
        assertThat(frontend.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("all이나 ALL, null이 들어오면 모든 질문지를 조회한다.")
    void queryAll() {
        createQuestion(QuestionCategory.BACKEND);
        createQuestion(QuestionCategory.BACKEND);
        createQuestion(QuestionCategory.FRONTEND);

        List<QuestionSummary> all = questionQueryService.queryAllByCategory("all");
        List<QuestionSummary> ALL = questionQueryService.queryAllByCategory("ALL");
        List<QuestionSummary> nul = questionQueryService.queryAllByCategory(null);

        assertThat(all.size()).isEqualTo(3);
        assertThat(ALL.size()).isEqualTo(3);
        assertThat(nul.size()).isEqualTo(3);
    }

    private Question createQuestion(QuestionCategory category) {
        Question question = new Question("test", "test", category);

        return questionRepository.save(question);
    }
}
