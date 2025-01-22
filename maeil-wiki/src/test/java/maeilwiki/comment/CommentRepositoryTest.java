package maeilwiki.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.UUID;
import maeilwiki.member.Member;
import maeilwiki.member.MemberRepository;
import maeilwiki.support.IntegrationTestSupport;
import maeilwiki.wiki.Wiki;
import maeilwiki.wiki.WikiRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CommentRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private WikiRepository wikiRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("주어진 위키에 속하는 답변이 존재하는지 조회한다.")
    void existsComment() {
        Member member = createMember();
        Wiki wiki = createWiki(member);
        Wiki noCommentWiki = createWiki(member);
        createComment(member, wiki);
        Comment comment = createComment(member, noCommentWiki);
        comment.remove();

        assertAll(
                () -> assertThat(commentRepository.existsByWikiIdAndDeletedAtIsNull(wiki.getId())).isTrue(),
                () -> assertThat(commentRepository.existsByWikiIdAndDeletedAtIsNull(noCommentWiki.getId())).isFalse()
        );
    }

    private Member createMember() {
        Member member = new Member(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "GITHUB");

        return memberRepository.save(member);
    }

    private Wiki createWiki(Member member) {
        Wiki wiki = new Wiki("question", "backend", false, member);

        return wikiRepository.save(wiki);
    }

    private Comment createComment(Member member, Wiki wiki) {
        Comment comment = new Comment("answer", false, member, wiki);

        return commentRepository.save(comment);
    }
}
