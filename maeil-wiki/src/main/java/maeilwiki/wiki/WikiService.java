package maeilwiki.wiki;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import maeilwiki.comment.CommentRepository;
import maeilwiki.comment.CommentRequest;
import maeilwiki.comment.CommentService;
import maeilwiki.member.Identity;
import maeilwiki.member.Member;
import maeilwiki.member.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class WikiService {

    private final WikiRepository wikiRepository;
    private final CommentRepository commentRepository;
    private final CommentService commentService;
    private final MemberService memberService;

    @Transactional
    public void create(Identity identity, WikiRequest request) {
        Member member = memberService.findById(identity.id());
        Wiki wiki = request.toWiki(member);

        wikiRepository.save(wiki);
    }

    @Transactional
    public void remove(Identity identity, Long wikiId) {
        Wiki wiki = wikiRepository.findById(wikiId)
                .orElseThrow(NoSuchElementException::new);
        validateOwner(identity, wiki);
        validateHasComment(wikiId);

        wiki.remove();
    }

    private void validateOwner(Identity identity, Wiki wiki) {
        Member owner = wiki.getMember();

        if (!identity.canAccessToResource(owner.getId())) {
            throw new IllegalStateException("자신의 위키만 삭제할 수 있습니다.");
        }
    }

    private void validateHasComment(Long wikiId) {
        boolean hasComment = commentRepository.existsByWikiIdAndDeletedAtIsNull(wikiId);
        if (hasComment) {
            throw new IllegalStateException("답변이 존재하는 위키는 삭제할 수 없습니다.");
        }
    }

    @Transactional
    public void comment(Identity identity, CommentRequest request, Long wikiId) {
        Wiki wiki = wikiRepository.findByIdAndDeletedAtIsNull(wikiId)
                .orElseThrow(NoSuchElementException::new);

        commentService.comment(identity, request, wiki.getId());
    }
}
