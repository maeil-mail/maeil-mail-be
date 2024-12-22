package maeilmail.admin;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
class AdminApi {

    private final AdminQuestionService adminQuestionService;
    private final AdminNoticeService adminNoticeService;
    private final AdminNoticeRepository adminNoticeRepository;

    @PutMapping("/admin/question")
    public ResponseEntity<Void> putQuestion(@RequestBody AdminQuestionRequest request) {
        if (request.isUpdate()) {
            adminQuestionService.updateQuestion(request.toQuestion());
        } else {
            adminQuestionService.createQuestion(request.toQuestion());
        }

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/admin/notice")
    public ResponseEntity<Void> putNotice(@RequestBody AdminNoticeRequest request) {
        if (request.isUpdate()) {
            adminNoticeService.updateNotice(request.toAdminNotice());
        } else {
            adminNoticeService.createNotice(request.toAdminNotice());
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/notice")
    public ResponseEntity<List<AdminNoticeResponse>> getNotice() {
        List<AdminNoticeResponse> responses = adminNoticeRepository.queryAll();

        return ResponseEntity.ok(responses);
    }
}
