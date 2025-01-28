package maeilwiki.comment.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.UUID;
import maeilwiki.comment.dto.CommentSummary;
import maeilwiki.member.domain.Member;
import maeilwiki.member.domain.MemberRepository;
import maeilwiki.support.IntegrationTestSupport;
import maeilwiki.wiki.domain.Wiki;
import maeilwiki.wiki.domain.WikiRepository;
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

    @Test
    @DisplayName("위키의 댓글을 조회한다.")
    void queryAllByWikiId() {
        // given
        Member prin = createMember();
        Member atom = createMember();

        Wiki wiki1 = createWiki(prin);
        Wiki wiki2 = createWiki(prin);

        Comment comment1 = createComment(atom, wiki1);
        Comment comment2 = createComment(atom, wiki1);
        createComment(atom, wiki2);

        // when
        List<CommentSummary> commentSummary = commentRepository.queryAllByWikiId(wiki1.getId());

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(commentSummary).hasSize(2);

            softAssertions.assertThat(commentSummary.get(0).id()).isEqualTo(comment1.getId());
            softAssertions.assertThat(commentSummary.get(0).answer()).isEqualTo(comment1.getAnswer());
            softAssertions.assertThat(commentSummary.get(0).isAnonymous()).isEqualTo(comment1.isAnonymous());
            softAssertions.assertThat(commentSummary.get(0).createdAt()).isEqualTo(comment1.getCreatedAt());
            softAssertions.assertThat(commentSummary.get(0).owner().name()).isEqualTo(comment1.getMember().getName());
            softAssertions.assertThat(commentSummary.get(0).owner().profileImage()).isEqualTo(comment1.getMember().getProfileImageUrl());
            softAssertions.assertThat(commentSummary.get(0).owner().github()).isEqualTo(comment1.getMember().getGithubUrl());

            softAssertions.assertThat(commentSummary.get(1).id()).isEqualTo(comment2.getId());
            softAssertions.assertThat(commentSummary.get(1).answer()).isEqualTo(comment2.getAnswer());
            softAssertions.assertThat(commentSummary.get(1).isAnonymous()).isEqualTo(comment2.isAnonymous());
            softAssertions.assertThat(commentSummary.get(1).createdAt()).isEqualTo(comment2.getCreatedAt());
            softAssertions.assertThat(commentSummary.get(1).owner().name()).isEqualTo(comment2.getMember().getName());
            softAssertions.assertThat(commentSummary.get(1).owner().profileImage()).isEqualTo(comment2.getMember().getProfileImageUrl());
            softAssertions.assertThat(commentSummary.get(1).owner().github()).isEqualTo(comment2.getMember().getGithubUrl());
        });
    }

    @Test
    @DisplayName("위키에 댓글이 없는 경우 빈 리스트를 반환한다.")
    void emptyResult() {
        // given
        Member atom = createMember();
        Wiki wiki = createWiki(atom);

        // when
        List<CommentSummary> commentSummary = commentRepository.queryAllByWikiId(wiki.getId());

        // then
        assertThat(commentSummary).isEmpty();
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
