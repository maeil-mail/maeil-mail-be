package maeilwiki.wiki;

import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import maeilsupport.PaginationResponse;
import maeilwiki.comment.CommentRepository;
import maeilwiki.comment.CommentRequest;
import maeilwiki.comment.CommentService;
import maeilwiki.comment.application.CommentResponse;
import maeilwiki.comment.dto.CommentSummary;
import maeilwiki.member.Identity;
import maeilwiki.member.Member;
import maeilwiki.member.MemberService;
import maeilwiki.wiki.application.response.WikiResponse;
import maeilwiki.wiki.dto.WikiSummary;
import maeilwiki.wiki.dto.WikiSummaryWithCommentCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Transactional(readOnly = true)
    public WikiResponse getWikiById(Long wikiId) {
        WikiSummary wikiSummary = resolveAnonymousWiki(wikiRepository.queryOneById(wikiId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 위키입니다.")));
        List<CommentResponse> commentResponses = commentRepository.queryAllByWikiId(wikiId)
                .stream()
                .map(this::resolveAnonymousComment)
                .map(CommentResponse::from)
                .toList();

        return WikiResponse.withComments(wikiSummary, commentResponses);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<WikiResponse> pageByCategory(String category, Pageable pageable) {
        Page<WikiSummaryWithCommentCount> pageResults = wikiRepository.pageByCategory(category, pageable);
        List<WikiResponse> wikiResponses = pageResults.getContent()
                .stream()
                .map(w -> WikiResponse.withCommentCount(resolveAnonymousWiki(w.wikiSummary()), w.commentCount()))
                .toList();

        return new PaginationResponse<>(pageResults.isLast(), (long) pageResults.getTotalPages(), wikiResponses);
    }

    private WikiSummary resolveAnonymousWiki(WikiSummary wikiSummary) {
        if (wikiSummary.isAnonymous()) {
            return wikiSummary.toAnonymousOwner();
        }
        return wikiSummary;
    }

    private CommentSummary resolveAnonymousComment(CommentSummary commentSummary) {
        if (commentSummary.isAnonymous()) {
            return commentSummary.toAnonymousOwner();
        }
        return commentSummary;
    }
}
