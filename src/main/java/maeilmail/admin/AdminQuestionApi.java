package maeilmail.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class AdminQuestionApi {

    private final AdminQuestionService adminQuestionService;

    @PostMapping("/admin/question")
    public ResponseEntity<Void> createQuestion(@RequestBody CreateQuestionRequest request) {
        adminQuestionService.createQuestion(request.toDomainEntity());

        return ResponseEntity.noContent().build();
    }
}
