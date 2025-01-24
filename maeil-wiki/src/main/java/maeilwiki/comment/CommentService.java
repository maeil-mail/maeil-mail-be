package maeilwiki.comment;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import maeilwiki.member.Member;
import maeilwiki.member.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final CommentLikeRepository commentLikeRepository;

    private final Map<String, Member> transactionTmpMemberMap = new ConcurrentHashMap<>();

    @Transactional
    public void comment(CommentRequest request, Long wikiId) {
        String uuid = UUID.randomUUID().toString();
        Member temporalMember = new Member(uuid, uuid, "GITHUB");
        memberRepository.save(temporalMember);
        Comment comment = request.toComment(temporalMember, wikiId);

        commentRepository.save(comment);
    }

    @Transactional
    public void remove(Long commentId) {
        // TODO: member 소유인지 확인해야한다.
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(NoSuchElementException::new);

        comment.remove();
    }

    @Transactional
    public void toggleLike(Long id) {
        Member member = memberSetting();
        Comment comment = commentRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);

        commentLikeRepository.findByCommentIdAndMemberId(comment.getId(), member.getId())
                .ifPresentOrElse(this::unlike, () -> like(member, comment));
    }

    // TODO: 인가 적용 시 제거
    private Member memberSetting() {
        String key = TransactionSynchronizationManager.getCurrentTransactionName();
        Member member = transactionTmpMemberMap.get(key);
        if (member == null) {
            String uuid = UUID.randomUUID().toString();
            Member newMember = new Member(uuid, uuid, "GITHUB");
            memberRepository.save(newMember);
            transactionTmpMemberMap.put(key, newMember);
            return newMember;
        }

        return member;
    }

    private void unlike(CommentLike commentLike) {
        commentLikeRepository.delete(commentLike);
    }

    private void like(Member member, Comment comment) {
        CommentLike commentLike = new CommentLike(member, comment);
        commentLikeRepository.save(commentLike);
    }
}
