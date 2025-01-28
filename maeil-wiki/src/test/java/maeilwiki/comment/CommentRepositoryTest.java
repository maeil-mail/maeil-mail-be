package maeilwiki.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import maeilwiki.comment.dto.CommentSummary;
import maeilwiki.member.Member;
import maeilwiki.member.MemberRepository;
import maeilwiki.support.RepositoryTestSupport;
import maeilwiki.wiki.Wiki;
import maeilwiki.wiki.WikiRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CommentRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WikiRepository wikiRepository;

    @Test
    @DisplayName("위키의 댓글을 조회한다.")
    void queryAllByWikiId() {
        // given
        Member prin = memberRepository.save(new Member("prin", "UUID1", "GITHUB"));
        Member atom = memberRepository.save(new Member("atom", "UUID2", "GITHUB"));

        Wiki wiki1 = wikiRepository.save(new Wiki("질문1", "FRONTEND", false, prin));
        Wiki wiki2 = wikiRepository.save(new Wiki("질문2", "BACKEND", true, prin));

        Comment comment1 = commentRepository.save(new Comment("답변1", true, atom, wiki1));
        Comment comment2 = commentRepository.save(new Comment("답변2", true, atom, wiki1));
        Comment comment3 = commentRepository.save(new Comment("답변3", true, atom, wiki2));

        // when
        List<CommentSummary> commentSummary = commentRepository.queryAllByWikiId(wiki1.getId());

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(commentSummary).hasSize(2);

            softAssertions.assertThat(commentSummary.get(0).id()).isEqualTo(comment1.getId());
            softAssertions.assertThat(commentSummary.get(0).answer()).isEqualTo(comment1.getAnswer());
            softAssertions.assertThat(commentSummary.get(0).owner().name()).isEqualTo(comment1.getMember().getName());
            softAssertions.assertThat(commentSummary.get(0).owner().profileImageUrl()).isEqualTo(comment1.getMember().getProfileImageUrl());
            softAssertions.assertThat(commentSummary.get(0).owner().github()).isEqualTo(comment1.getMember().getGithubUrl());
            softAssertions.assertThat(commentSummary.get(0).createdAt()).isEqualTo(comment1.getCreatedAt());

            softAssertions.assertThat(commentSummary.get(1).id()).isEqualTo(comment2.getId());
            softAssertions.assertThat(commentSummary.get(1).answer()).isEqualTo(comment2.getAnswer());
            softAssertions.assertThat(commentSummary.get(1).owner().name()).isEqualTo(comment2.getMember().getName());
            softAssertions.assertThat(commentSummary.get(1).owner().profileImageUrl()).isEqualTo(comment2.getMember().getProfileImageUrl());
            softAssertions.assertThat(commentSummary.get(1).owner().github()).isEqualTo(comment2.getMember().getGithubUrl());
            softAssertions.assertThat(commentSummary.get(1).createdAt()).isEqualTo(comment2.getCreatedAt());
        });
    }

    @Test
    @DisplayName("위키에 댓글이 없는 경우 빈 리스트를 반환한다.")
    void emptyResult() {
        // given
        Member atom = memberRepository.save(new Member("atom", "UUID", "GITHUB"));
        Wiki wiki = wikiRepository.save(new Wiki("질문1", "FRONTEND", false, atom));

        // when
        List<CommentSummary> commentSummary = commentRepository.queryAllByWikiId(wiki.getId());

        // then
        assertThat(commentSummary).isEmpty();
    }
}
