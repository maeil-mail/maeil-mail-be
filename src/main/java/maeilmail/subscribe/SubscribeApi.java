package maeilmail.subscribe;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class SubscribeApi {

    private final SubscribeService subscribeService;
    private final UnsubscribeService unsubscribeService;
    private final ChangeFrequencyService changeFrequencyService;

    @PostMapping("/subscribe/verify/send")
    public ResponseEntity<Void> send(@RequestBody VerifyEmailRequest request) {
        subscribeService.sendCodeIncludedMail(request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@RequestBody SubscribeRequest request) {
        subscribeService.subscribe(request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/subscribe/email-frequency")
    public ResponseEntity<Void> changeFrequency(@RequestBody ChangeFrequencyRequest request) {
        changeFrequencyService.changeFrequency(request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/subscribe")
    public ResponseEntity<Void> unsubscribe(@RequestBody UnsubscribeRequest request) {
        unsubscribeService.unsubscribe(request);

        return ResponseEntity.noContent().build();
    }
}
