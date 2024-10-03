package maeilmail.question;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class QuestionApi {

    private final QuestionRepository questionRepository;

    @GetMapping("/question")
    public ResponseEntity<List<QuestionSummary>> getQuestions(
            @RequestParam(defaultValue = "all") String category
    ) {
        List<QuestionSummary> summaries = questionRepository.queryAll(category);

        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/question/{id}")
    public ResponseEntity<QuestionSummary> getQuestionById(@PathVariable Long id) {
        QuestionSummary summary = questionRepository.queryById(id);

        return ResponseEntity.ok(summary);
    }
}
