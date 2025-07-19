package maeilmail.api;

import lombok.RequiredArgsConstructor;
import maeilmail.subscribe.command.application.SubscribeService;
import maeilmail.subscribe.command.application.TransmissionFrequencyService;
import maeilmail.subscribe.command.application.UnsubscribeService;
import maeilmail.subscribe.command.application.request.SubscribeRequest;
import maeilmail.subscribe.command.application.request.TransmissionFrequencyRequest;
import maeilmail.subscribe.command.application.request.UnsubscribeRequest;
import maeilmail.subscribe.command.application.request.VerifyEmailRequest;
import maeilmail.subscribe.command.application.response.TransmissionFrequencyResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class SubscribeApi {

    private final SubscribeService subscribeService;
    private final UnsubscribeService unsubscribeService;
    private final TransmissionFrequencyService transmissionFrequencyService;

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

    @DeleteMapping("/subscribe")
    public ResponseEntity<Void> unsubscribe(@RequestBody UnsubscribeRequest request) {
        unsubscribeService.unsubscribe(request);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/subscribe/email-frequency")
    public ResponseEntity<Void> changeFrequency(@RequestBody TransmissionFrequencyRequest request) {
        transmissionFrequencyService.changeFrequency(request);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subscribe/email-frequency")
    public ResponseEntity<TransmissionFrequencyResponse> getFrequency(@RequestParam String email) {
        TransmissionFrequencyResponse response = transmissionFrequencyService.getFrequency(email);

        return ResponseEntity.ok(response);
    }
}
