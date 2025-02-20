package maeilmail.subscribe.api;

import lombok.RequiredArgsConstructor;
import maeilmail.subscribe.query.SubscribeQuestionQueryService;
import maeilmail.subscribe.query.SubscribeQuestionSummary;
import maeilmail.subscribe.query.WeeklySubscribeQuestionResponse;
import maeilsupport.PaginationResponse;
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
            @PageableDefault Pageable pageable
    ) {
        PaginationResponse<SubscribeQuestionSummary> response = subscribeQuestionQueryService.pageByEmailAndCategory(email, category, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/subscribe-question/weekly")
    public ResponseEntity<WeeklySubscribeQuestionResponse> getSubscribeQuestion(
            @RequestParam String email,
            @RequestParam String category,
            @RequestParam Long year,
            @RequestParam Long month,
            @RequestParam Long week
    ) {
        WeeklySubscribeQuestionResponse response = subscribeQuestionQueryService.queryWeeklyQuestions(email, category, year, month, week);

        return ResponseEntity.ok(response);
    }
}
