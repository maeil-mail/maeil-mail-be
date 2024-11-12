package maeilmail.subscribequestion;

import lombok.RequiredArgsConstructor;
import maeilmail.PaginationResponse;
import maeilmail.question.QuestionSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SubscribeQuestionApi {

    private final SubscribeQuestionQueryService subscribeQuestionQueryService;

    @GetMapping("/subscribe-question")
    public ResponseEntity<PaginationResponse<QuestionSummary>> getSubscribeQuestion(
            @RequestParam String email,
            @RequestParam(defaultValue = "all") String category,
            @PageableDefault(sort = {"category", "id"}) Pageable pageable
    ) {
        PaginationResponse<QuestionSummary> response = subscribeQuestionQueryService.pageByEmailAndCategory(email, category, pageable);

        return ResponseEntity.ok(response);
    }
}
