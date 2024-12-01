package maeilmail.subscribequestion;

import lombok.RequiredArgsConstructor;
import maeilmail.PaginationResponse;
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
    public ResponseEntity<PaginationResponse<SubscribeQuestionSummary>> getSubscribeQuestion(
            @RequestParam String email,
            @RequestParam(defaultValue = "all") String category,
            @PageableDefault(sort = {"category", "id"}, size = 200) Pageable pageable
    ) {
        PaginationResponse<SubscribeQuestionSummary> response = subscribeQuestionQueryService.pageByEmailAndCategory(email, category, pageable);

        return ResponseEntity.ok(response);
    }
}
