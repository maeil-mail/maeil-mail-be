package maeilwiki.comment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import maeilwiki.comment.domain.Comment;
import maeilwiki.comment.domain.CommentLike;
import maeilwiki.comment.domain.CommentLikeRepository;
import maeilwiki.comment.domain.CommentRepository;
import maeilwiki.member.Identity;
import maeilwiki.member.domain.Member;
import maeilwiki.member.domain.MemberRepository;
import maeilwiki.support.IntegrationTestSupport;
import maeilwiki.wiki.domain.Wiki;
import maeilwiki.wiki.domain.WikiRepository;
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
    @DisplayName("존재하지 않는 답변을 삭제할 수 없다.")
    void notFoundComment() {
        Long unknownCommentId = -1L;
        Identity identity = new Identity(1L);

        assertThatThrownBy(() -> commentService.remove(identity, unknownCommentId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("자신의 답변만 삭제할 수 있다.")
    void cantRemoveOtherComment() {
        Member member = createMember();
        Wiki wiki = createWiki(member);
        Comment comment = createComment(member, wiki);
        Member otherMember = createMember();
        Identity otherMemberIdentity = new Identity(otherMember.getId());

        assertThatThrownBy(() -> commentService.remove(otherMemberIdentity, comment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("자신의 답변만 삭제할 수 있습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 답변에 좋아요를 생성할 수 없다.")
    void notFoundCommentForLike() {
        Long unknownCommentId = -1L;
        Identity identity = new Identity(1L);

        assertThatThrownBy(() -> commentService.toggleLike(identity, unknownCommentId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("답변에 좋아요를 생성할 수 있다.")
    void like() {
        Member member = createMember();
        Wiki wiki = createWiki(member);
        Comment comment = createComment(member, wiki);
        Identity identity = new Identity(member.getId());

        commentService.toggleLike(identity, comment.getId());

        List<CommentLike> likes = commentLikeRepository.findAll();
        assertThat(likes).hasSize(1);
    }

    @Test
    @DisplayName("이미 해당 사용자가 좋아요를 생성한 상황에서 재요청하는 경우, 좋아요를 취소한다.")
    void unlike() {
        Member member = createMember();
        Wiki wiki = createWiki(member);
        Comment comment = createComment(member, wiki);
        Identity identity = new Identity(member.getId());
        commentService.toggleLike(identity, comment.getId());

        commentService.toggleLike(identity, comment.getId());

        List<CommentLike> likes = commentLikeRepository.findAll();
        assertThat(likes).hasSize(0);
    }

    private Member createMember() {
        Member member = new Member(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "GITHUB");
        member.setRefreshToken("refresh");

        return memberRepository.save(member);
    }

    private Wiki createWiki(Member member) {
        Wiki wiki = new Wiki("question", "backend", false, member);

        return wikiRepository.save(wiki);
    }

    private Comment createComment(Member member, Wiki wiki) {
        Comment comment = new Comment("answer", false, member, wiki.getId());

        return commentRepository.save(comment);
    }
}
