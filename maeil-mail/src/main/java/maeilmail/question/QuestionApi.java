package maeilmail.question;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuestionApi {

    private final QuestionQueryService questionQueryService;

    @GetMapping("/question/{id}")
    public ResponseEntity<QuestionSummary> getQuestionById(@PathVariable Long id) {
        QuestionSummary summary = questionQueryService.queryOneById(id);

        return ResponseEntity.ok(summary);
    }
}
