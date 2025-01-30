package maeilwiki.wiki.application;

import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import maeilsupport.PaginationResponse;
import maeilwiki.comment.application.CommentRequest;
import maeilwiki.comment.application.CommentResponse;
import maeilwiki.comment.application.CommentService;
import maeilwiki.comment.domain.CommentRepository;
import maeilwiki.comment.dto.CommentSummary;
import maeilwiki.member.application.MemberIdentity;
import maeilwiki.member.application.MemberService;
import maeilwiki.member.domain.Member;
import maeilwiki.wiki.domain.Wiki;
import maeilwiki.wiki.domain.WikiRepository;
import maeilwiki.wiki.dto.WikiSummary;
import maeilwiki.wiki.dto.WikiSummaryWithCommentCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WikiService {

    private final WikiRepository wikiRepository;
    private final CommentRepository commentRepository;
    private final CommentService commentService;
    private final MemberService memberService;

    @Transactional
    public void create(MemberIdentity identity, WikiRequest request) {
        Member member = memberService.findById(identity.id());
        Wiki wiki = request.toWiki(member);

        wikiRepository.save(wiki);
    }

    @Transactional
    public void remove(MemberIdentity identity, Long wikiId) {
        Wiki wiki = wikiRepository.findById(wikiId)
                .orElseThrow(NoSuchElementException::new);
        validateOwner(identity, wiki);
        validateHasComment(wikiId);

        wiki.remove();
    }

    private void validateOwner(MemberIdentity identity, Wiki wiki) {
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
    public void comment(MemberIdentity identity, CommentRequest request, Long wikiId) {
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
                .map(it -> WikiResponse.withCommentCount(resolveAnonymousWiki(it.wikiSummary()), it.commentCount()))
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
