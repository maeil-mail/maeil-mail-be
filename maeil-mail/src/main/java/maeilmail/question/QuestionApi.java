package maeilmail.question;

import lombok.RequiredArgsConstructor;
import maeilsupport.PaginationResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuestionApi {

    private final QuestionQueryService questionQueryService;

    @GetMapping("/question")
    public ResponseEntity<PaginationResponse<QuestionSummary>> getQuestions(
            @RequestParam(defaultValue = "all") String category,
            @PageableDefault Pageable pageable
    ) {
        PaginationResponse<QuestionSummary> response = questionQueryService.pageByCategory(category, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/question/{id}")
    public ResponseEntity<QuestionSummary> getQuestionById(@PathVariable Long id) {
        QuestionSummary summary = questionQueryService.queryOneById(id);

        return ResponseEntity.ok(summary);
    }
}
