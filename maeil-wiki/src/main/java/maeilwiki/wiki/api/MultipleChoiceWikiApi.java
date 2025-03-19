package maeilwiki.wiki.api;

import lombok.RequiredArgsConstructor;
import maeilsupport.PaginationResponse;
import maeilwiki.member.api.NotRequiredIdentity;
import maeilwiki.member.application.MemberIdentity;
import maeilwiki.wiki.application.MultipleChoiceWikiRequest;
import maeilwiki.wiki.application.MultipleChoiceWikiResponse;
import maeilwiki.wiki.application.MultipleChoiceWikiService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.net.URI;

@RestController
@RequiredArgsConstructor
class MultipleChoiceWikiApi {

    private final MultipleChoiceWikiService multipleChoiceWikiService;

    @PostMapping("/wiki/multiple-choice")
    public ResponseEntity<Void> createWiki(MemberIdentity identity, @RequestBody MultipleChoiceWikiRequest request) {
        Long resourceId = multipleChoiceWikiService.create(identity, request);
        URI location = URI.create(resourceId.toString());

        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/wiki/multiple-choice/{id}")
    public ResponseEntity<Void> deleteWiki(MemberIdentity identity, @PathVariable Long id) {
        multipleChoiceWikiService.remove(identity, id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/wiki/multiple-choice/{id}")
    public ResponseEntity<MultipleChoiceWikiResponse> getWiki(@NotRequiredIdentity MemberIdentity identity, @PathVariable Long id) {
        MultipleChoiceWikiResponse wiki = multipleChoiceWikiService.getWikiById(identity, id);

        return ResponseEntity.ok(wiki);
    }

    @GetMapping("/wiki/multiple-choice")
    public ResponseEntity<PaginationResponse<MultipleChoiceWikiResponse>> getWikis(
            @RequestParam(defaultValue = "all") String category,
            @PageableDefault Pageable pageable
    ) {
        PaginationResponse<MultipleChoiceWikiResponse> response = multipleChoiceWikiService.pageByCategory(category, pageable);

        return ResponseEntity.ok(response);
    }
}
