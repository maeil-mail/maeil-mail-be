package maeilwiki.comment;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import maeilwiki.member.Identity;
import maeilwiki.member.Member;
import maeilwiki.member.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final MemberService memberService;

    @Transactional
    public void comment(Identity identity, CommentRequest request, Long wikiId) {
        Member member = memberService.findById(identity.id());
        Comment comment = request.toComment(member, wikiId);

        commentRepository.save(comment);
    }

    @Transactional
    public void remove(Identity identity, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(NoSuchElementException::new);
        validateOwner(identity, comment);

        comment.remove();
    }

    private void validateOwner(Identity identity, Comment comment) {
        Member owner = comment.getMember();

        if (!identity.canAccessToResource(owner.getId())) {
            throw new IllegalStateException("자신의 답변만 삭제할 수 있습니다.");
        }
    }

    @Transactional
    public void toggleLike(Identity identity, Long id) {
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
}
