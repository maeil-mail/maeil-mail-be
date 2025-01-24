package maeilwiki.wiki;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;
import java.util.UUID;
import maeilwiki.comment.Comment;
import maeilwiki.comment.CommentRepository;
import maeilwiki.comment.CommentRequest;
import maeilwiki.member.Member;
import maeilwiki.member.MemberRepository;
import maeilwiki.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class WikiServiceTest extends IntegrationTestSupport {

    @Autowired
    private WikiRepository wikiRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private WikiService wikiService;

    @Test
    @DisplayName("존재하지 않는 위키에 답변을 작성할 수 없다.")
    void notfound() {
        CommentRequest request = new CommentRequest("답변을 작성합니다.", false);
        Long unknownWikiId = -1L;

        assertThatThrownBy(() -> wikiService.comment(request, unknownWikiId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("답변이 존재하는 위키는 삭제할 수 없다.")
    void cantRemove() {
        Member member = createMember();
        Wiki wiki = createWiki(member);
        Comment comment = createComment(member, wiki);

        assertThatThrownBy(() -> wikiService.remove(wiki.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("답변이 존재하는 위키는 삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 위키는 삭제할 수 없다.")
    void cantRemoveUnknownWiki() {
        Long unknownWikiId = -1L;

        assertThatThrownBy(() -> wikiService.remove(unknownWikiId))
                .isInstanceOf(NoSuchElementException.class);
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
