package maeilwiki.wiki;

import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import maeilwiki.comment.CommentRepository;
import maeilwiki.comment.CommentRequest;
import maeilwiki.comment.CommentService;
import maeilwiki.member.Member;
import maeilwiki.member.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class WikiService {

    private final WikiRepository wikiRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final CommentService commentService;

    @Transactional
    public void create(WikiRequest request) {
        String uuid = UUID.randomUUID().toString();
        Member temporalMember = new Member(uuid, uuid, "GITHUB");
        memberRepository.save(temporalMember);
        Wiki wiki = request.toWiki(temporalMember); // TODO : 로그인 구현

        wikiRepository.save(wiki);
    }

    @Transactional
    public void remove(Long wikiId) {
        // TODO : member 소유인지 확인해야한다.
        validateHasComment(wikiId);
        Wiki wiki = wikiRepository.findById(wikiId)
                .orElseThrow(NoSuchElementException::new);

        wiki.remove();
    }

    @Transactional
    public void comment(CommentRequest request, Long wikiId) {
        Wiki wiki = wikiRepository.findByIdAndDeletedAtIsNull(wikiId)
                .orElseThrow(NoSuchElementException::new);

        commentService.comment(request, wiki.getId());
    }

    private void validateHasComment(Long wikiId) {
        boolean hasComment = commentRepository.existsByWikiIdAndDeletedAtIsNull(wikiId);
        if (hasComment) {
            throw new IllegalStateException("답변이 존재하는 위키는 삭제할 수 없습니다.");
        }
    }
}
