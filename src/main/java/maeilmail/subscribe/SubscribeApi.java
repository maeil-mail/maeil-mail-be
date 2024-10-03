package maeilmail.subscribe;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class SubscribeApi {

    private final SubscribeQuestionService subscribeQuestionService;

    @PostMapping("/subscribe/verify/send")
    public ResponseEntity<Void> send(@RequestBody VerifyEmailRequest request) {
        subscribeQuestionService.sendCodeIncludedMail(request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@RequestBody SubscribeQuestionRequest request) {
        subscribeQuestionService.subscribe(request);

        return ResponseEntity.noContent().build();
    }
}
