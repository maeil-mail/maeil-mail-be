package maeilwiki.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    /**
     * wikiId는 현재 사용 계획이 없지만, 입력 받는 이유는 다음과 같습니다.
     * - 나중에 특정 위키가 아카이브 상태로 전환되는 기능을 구현할 때는 필요할 수 있습니다.
     * - comment가 wiki에 속하는 개념이라는 것을 uri로 표현할 수 있습니다.
     */
    @DeleteMapping("/wiki/{wikiId}/comment/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long wikiId, @PathVariable Long id) {
        commentService.remove(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/wiki/{wikiId}/comment/{id}/like")
    public ResponseEntity<Void> createCommentLike(@PathVariable Long wikiId, @PathVariable Long id) {
        commentService.toggleLike(id);

        return ResponseEntity.noContent().build();
    }
}
