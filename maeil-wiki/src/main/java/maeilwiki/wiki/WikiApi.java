package maeilwiki.wiki;

import lombok.RequiredArgsConstructor;
import maeilsupport.PaginationResponse;
import maeilwiki.wiki.application.response.WikiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/wiki")
    public ResponseEntity<PaginationResponse<WikiResponse>> getWikis(
            @RequestParam(defaultValue = "all") String category,
            @PageableDefault Pageable pageable
    ) {
        PaginationResponse<WikiResponse> response = wikiService.pageByCategory(category, pageable);

        return ResponseEntity.ok(response);
    }
}
