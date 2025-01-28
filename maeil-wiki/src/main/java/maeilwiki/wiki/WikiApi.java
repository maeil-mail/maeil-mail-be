package maeilwiki.wiki;

import lombok.RequiredArgsConstructor;
import maeilsupport.PaginationResponse;
import maeilwiki.comment.CommentRequest;
import maeilwiki.comment.CommentService;
import maeilwiki.member.Identity;
import maeilwiki.wiki.application.response.WikiResponse;
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

@RestController
@RequiredArgsConstructor
class WikiApi {

    private final WikiService wikiService;
    private final CommentService commentService;

    @PostMapping("/wiki")
    public ResponseEntity<Void> createWiki(Identity identity, @RequestBody WikiRequest request) {
        wikiService.create(identity, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/wiki/{id}")
    public ResponseEntity<Void> deleteWiki(Identity identity, @PathVariable Long id) {
        wikiService.remove(identity, id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/wiki/{wikiId}/comment")
    public ResponseEntity<Void> createComment(
            Identity identity,
            @RequestBody CommentRequest request,
            @PathVariable Long wikiId
    ) {
        wikiService.comment(identity, request, wikiId);

        return ResponseEntity.noContent().build();
    }

    /**
     * wikiId는 현재 사용 계획이 없지만, 입력 받는 이유는 다음과 같습니다.
     * - 나중에 특정 위키가 아카이브 상태로 전환되는 기능을 구현할 때는 필요할 수 있습니다.
     * - comment가 wiki에 속하는 개념이라는 것을 uri로 표현할 수 있습니다.
     */
    @DeleteMapping("/wiki/{wikiId}/comment/{id}")
    public ResponseEntity<Void> deleteComment(
            Identity identity,
            @PathVariable Long wikiId,
            @PathVariable Long id
    ) {
        commentService.remove(identity, id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/wiki/{wikiId}/comment/{id}/like")
    public ResponseEntity<Void> toggleLike(
            Identity identity,
            @PathVariable Long wikiId,
            @PathVariable Long id
    ) {
        commentService.toggleLike(identity, id);

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
