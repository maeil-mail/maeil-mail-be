package maeilwiki.mutiplechoice.api;

import lombok.RequiredArgsConstructor;
import maeilwiki.member.application.MemberIdentity;
import maeilwiki.mutiplechoice.application.MultipleChoiceService;
import maeilwiki.mutiplechoice.application.WorkbookCreatedResponse;
import maeilwiki.mutiplechoice.application.WorkbookRequest;
import maeilwiki.mutiplechoice.application.WorkbookResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class MultipleChoiceApi {

    private final MultipleChoiceService multipleChoiceService;

    @PostMapping("/wiki/multiple-choice")
    public ResponseEntity<WorkbookCreatedResponse> createWorkbook(MemberIdentity identity, @RequestBody WorkbookRequest request) {
        WorkbookCreatedResponse response = multipleChoiceService.create(identity, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/wiki/multiple-choice/{id}")
    public ResponseEntity<WorkbookResponse> getWiki(@PathVariable Long id) {
        WorkbookResponse response = multipleChoiceService.getWorkbookById(id);

        return ResponseEntity.ok(response);
    }
}
