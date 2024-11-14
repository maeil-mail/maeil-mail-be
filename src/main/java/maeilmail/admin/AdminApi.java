package maeilmail.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
class AdminApi {

    private final AdminQuestionService adminQuestionService;

    @PutMapping("/admin/question")
    public ResponseEntity<Void> putQuestion(@RequestBody AdminQuestionRequest request) {
        if (request.isUpdate()) {
            adminQuestionService.updateQuestion(request.toQuestion());
        } else {
            adminQuestionService.createQuestion(request.toQuestion());
        }

        return ResponseEntity.noContent().build();
    }
}
