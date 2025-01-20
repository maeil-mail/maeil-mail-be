package maeilmail.admin;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/admin/notice/{id}/test")
    public ResponseEntity<Void> sendTest(@PathVariable Long id, @RequestBody AdminNoticeTestRequest request) {
        adminNoticeService.sendTest(id, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admin/notice/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        adminNoticeService.deleteNotice(id);

        return ResponseEntity.noContent().build();
    }
}
