package maeilwiki.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.NoSuchElementException;
import maeilwiki.member.Member;
import maeilwiki.member.MemberRepository;
import maeilwiki.support.IntegrationTestSupport;
import maeilwiki.wiki.Wiki;
import maeilwiki.wiki.WikiRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CommentServiceTest extends IntegrationTestSupport {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WikiRepository wikiRepository;

    @Test
    @DisplayName("존재하지 않는 위키에 답변을 작성할 수 없다.")
    void notfound() {
        CommentRequest request = new CommentRequest("답변을 작성합니다.", false);
        Long unknownWikiId = -1L;

        assertThatThrownBy(() -> commentService.comment(request, unknownWikiId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("존재하지 않는 답변을 삭제할 수 없다.")
    void notFoundComment() {
        Long unknownCommentId = -1L;

        assertThatThrownBy(() -> commentService.remove(unknownCommentId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("존재하지 않는 답변에 좋아요를 생성할 수 없다.")
    void notFoundCommentForLike() {
        Long unknownCommentId = -1L;

        assertThatThrownBy(() -> commentService.toggleLike(unknownCommentId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("답변에 좋아요를 생성할 수 있다.")
    void like() {
        Comment comment = createComment();

        commentService.toggleLike(comment.getId());

        List<CommentLike> likes = commentLikeRepository.findAll();
        assertThat(likes).hasSize(1);
    }

    @Test
    @DisplayName("이미 해당 사용자가 좋아요를 생성한 상황에서 재요청하는 경우, 좋아요를 취소한다.")
    void unlike() {
        Comment comment = createComment();
        commentService.toggleLike(comment.getId());

        commentService.toggleLike(comment.getId());

        List<CommentLike> likes = commentLikeRepository.findAll();
        assertThat(likes).hasSize(0);
    }

    private Comment createComment() {
        Member member = new Member("name", "providerId", "GITHUB");
        memberRepository.save(member);

        Wiki wiki = new Wiki("question", "backend", false, member);
        wikiRepository.save(wiki);

        Comment comment = new Comment("answer", false, member, wiki);
        return commentRepository.save(comment);
    }
}
