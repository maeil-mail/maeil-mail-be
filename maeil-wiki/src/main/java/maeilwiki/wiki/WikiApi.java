package maeilwiki.wiki;

import lombok.RequiredArgsConstructor;
import maeilwiki.wiki.application.response.WikiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/wiki/{id}")
    public ResponseEntity<WikiResponse> getWiki(@PathVariable Long id) {
        WikiResponse wiki = wikiService.getWikiById(id);

        return ResponseEntity.ok(wiki);
    }
}
