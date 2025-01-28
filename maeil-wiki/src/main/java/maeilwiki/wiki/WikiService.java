package maeilwiki.wiki;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import maeilsupport.PaginationResponse;
import maeilwiki.comment.CommentRepository;
import maeilwiki.comment.application.CommentResponse;
import maeilwiki.comment.dto.CommentSummary;
import maeilwiki.member.Member;
import maeilwiki.member.MemberRepository;
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
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void create(WikiRequest request) {
        String uuid = UUID.randomUUID().toString();
        Member temporalMember = new Member(uuid, uuid, "GITHUB");
        memberRepository.save(temporalMember);
        Wiki wiki = request.toWiki(temporalMember); // TODO : 로그인 구현

        wikiRepository.save(wiki);
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
