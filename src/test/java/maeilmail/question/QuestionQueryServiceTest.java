package maeilmail.question;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import maeilmail.PaginationResponse;
import maeilmail.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

class QuestionQueryServiceTest extends IntegrationTestSupport {

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

    @Test
    @DisplayName("페이징처리하여 질문지를 조회한다.")
    void pageByCategory() {
        List<Long> backendIds = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Question question = createQuestion(QuestionCategory.BACKEND);
            backendIds.add(question.getId());
            createQuestion(QuestionCategory.FRONTEND);
        }

        int pageSize = 3;
        List<Long> expectedIds = backendIds.subList(0, pageSize);
        PaginationResponse<QuestionSummary> response
                = questionQueryService.pageByCategory("backend", PageRequest.of(0, pageSize));

        assertAll(
                () -> assertThat(response.isLastPage()).isFalse(),
                () -> assertThat(response.data()).hasSize(3),
                () -> assertThat(response.data())
                        .map(QuestionSummary::id)
                        .containsExactlyElementsOf(expectedIds)
        );
    }

    private Question createQuestion(QuestionCategory category) {
        Question question = new Question("test", "test", category);

        return questionRepository.save(question);
    }
}
