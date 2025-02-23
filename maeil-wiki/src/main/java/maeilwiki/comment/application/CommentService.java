package maeilwiki.comment.application;

import lombok.RequiredArgsConstructor;
import maeilwiki.comment.domain.Comment;
import maeilwiki.comment.domain.CommentLike;
import maeilwiki.comment.domain.CommentLikeRepository;
import maeilwiki.comment.domain.CommentRepository;
import maeilwiki.comment.dto.CommentSummary;
import maeilwiki.member.application.MemberIdentity;
import maeilwiki.member.application.MemberService;
import maeilwiki.member.domain.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final MemberService memberService;

    @Transactional
    public void comment(MemberIdentity identity, CommentRequest request, Long wikiId) {
        Member member = memberService.findById(identity.id());
        Comment comment = request.toComment(member, wikiId);

        commentRepository.save(comment);
    }

    @Transactional
    public void remove(MemberIdentity identity, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(NoSuchElementException::new);
        validateOwner(identity, comment);

        comment.remove();
    }

    private void validateOwner(MemberIdentity identity, Comment comment) {
        Member owner = comment.getMember();

        if (!identity.canAccessToResource(owner.getId())) {
            throw new IllegalStateException("자신의 답변만 삭제할 수 있습니다.");
        }
    }

    @Transactional
    public void toggleLike(MemberIdentity identity, Long id) {
        Member member = memberService.findById(identity.id());
        Comment comment = commentRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);

        commentLikeRepository.findByCommentIdAndMemberId(comment.getId(), member.getId())
                .ifPresentOrElse(this::unlike, () -> like(member, comment));
    }

    private void unlike(CommentLike commentLike) {
        commentLikeRepository.delete(commentLike);
    }

    private void like(Member member, Comment comment) {
        CommentLike commentLike = new CommentLike(member, comment);
        commentLikeRepository.save(commentLike);
    }

    @Transactional(readOnly = true)
    public CommentResponses list(MemberIdentity identity, Long wikiId, Long cursorId, Long size) {
        List<CommentSummary> commentSummaries = commentRepository.queryByWikiIdAndIdGreaterThan(wikiId, identity.id(), cursorId, size);
        Long totalCount = commentRepository.countAllByWikiId(wikiId);

        return CommentResponses.of(commentSummaries, totalCount);
    }
}
