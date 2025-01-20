package maeilwiki.sample;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class SampleWikiApi {

    private final SampleWikiService service;

    @GetMapping("/sample/wiki")
    public ResponseEntity<String> sample() {
        return ResponseEntity.ok("sample");
    }
}
