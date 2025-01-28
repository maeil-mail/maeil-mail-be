package maeilwiki.wiki;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import maeilwiki.comment.CommentRepository;
import maeilwiki.comment.dto.CommentSummary;
import maeilwiki.member.Member;
import maeilwiki.member.MemberRepository;
import maeilwiki.wiki.application.response.WikiResponse;
import maeilwiki.wiki.dto.WikiSummary;
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
        List<CommentSummary> commentSummaries = commentRepository.queryAllByWikiId(wikiId)
                .stream()
                .map(this::resolveAnonymousComment)
                .toList();

        return WikiResponse.withComments(wikiSummary, commentSummaries);
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
