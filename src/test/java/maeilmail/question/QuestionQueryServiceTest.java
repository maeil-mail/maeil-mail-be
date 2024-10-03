package maeilmail.question;

import static org.assertj.core.api.Assertions.assertThat;

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
        Question question = new Question("test", "test", QuestionCategory.BACKEND);
        questionRepository.save(question);

        QuestionSummary result = questionQueryService.queryOneById(question.getId());

        assertThat(result.id()).isEqualTo(question.getId());
        assertThat(result.category()).isEqualTo("backend");
    }
}
