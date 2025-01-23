package maeilwiki.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class CommentApi {

    private final CommentService commentService;

    @PostMapping("/wiki/{wikiId}/comment")
    public ResponseEntity<Void> createComment(@RequestBody CommentRequest request, @PathVariable Long wikiId) {
        commentService.comment(request, wikiId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/wiki/{wikiId}/comment/{id}/like")
    public ResponseEntity<Void> createCommentLike(@PathVariable Long wikiId, @PathVariable Long id) {
        commentService.toggleLike(id);

        return ResponseEntity.noContent().build();
    }
}
