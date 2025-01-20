package maeilwiki.wiki;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class WikiApi {

    private final WikiService wikiService;

    @PostMapping("/wiki")
    public ResponseEntity<Void> createWiki(@RequestBody WikiRequest request) {
        wikiService.create(request);

        return ResponseEntity.noContent().build();
    }
}
