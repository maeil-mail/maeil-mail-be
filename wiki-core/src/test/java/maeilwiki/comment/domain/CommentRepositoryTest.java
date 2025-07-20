package maeilwiki.comment.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.UUID;
import maeilwiki.comment.dto.CommentSummary;
import maeilwiki.support.IntegrationTestSupport;
import maeilwiki.member.domain.Member;
import maeilwiki.member.domain.MemberRepository;
import maeilwiki.member.dto.MemberThumbnail;
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

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Test
    @DisplayName("주어진 위키에 속하는 답변이 존재하는지 조회한다.")
    void existsComment() {
        Member member = createMember();
        Wiki wiki = createWiki(member);
        Wiki noCommentWiki = createWiki(member);
        createComment(member, wiki);

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
        Member leesang = createMember();

        Wiki wiki1 = createWiki(prin);
        Comment wiki1comment1 = createComment(atom, wiki1);
        createCommentLike(prin, wiki1comment1);
        createCommentLike(leesang, wiki1comment1);
        Comment wiki1comment2 = createComment(atom, wiki1);

        Wiki wiki2 = createWiki(prin);
        Comment wiki2comment1 = createComment(atom, wiki2);
        createCommentLike(prin, wiki2comment1);

        // when
        List<CommentSummary> commentSummaries = commentRepository.queryAllByWikiId(wiki1.getId());

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(commentSummaries).hasSize(2);

            CommentSummary comment1 = commentSummaries.get(0);
            softAssertions.assertThat(comment1.id()).isEqualTo(wiki1comment1.getId());
            softAssertions.assertThat(comment1.answer()).isEqualTo(wiki1comment1.getAnswer());
            softAssertions.assertThat(comment1.isAnonymous()).isEqualTo(wiki1comment1.isAnonymous());
            softAssertions.assertThat(comment1.likeCount()).isEqualTo(2);
            softAssertions.assertThat(comment1.likeMemberIds()).contains(prin.getId(), leesang.getId());
            MemberThumbnail comment1Owner = comment1.owner();
            softAssertions.assertThat(comment1Owner.id()).isEqualTo(atom.getId());
            softAssertions.assertThat(comment1Owner.name()).isEqualTo(atom.getName());
            softAssertions.assertThat(comment1Owner.profileImage()).isEqualTo(atom.getProfileImageUrl());
            softAssertions.assertThat(comment1Owner.github()).isEqualTo(atom.getGithubUrl());

            CommentSummary comment2 = commentSummaries.get(1);
            softAssertions.assertThat(comment2.id()).isEqualTo(wiki1comment2.getId());
            softAssertions.assertThat(comment2.answer()).isEqualTo(wiki1comment2.getAnswer());
            softAssertions.assertThat(comment2.isAnonymous()).isEqualTo(wiki1comment2.isAnonymous());
            softAssertions.assertThat(comment2.likeCount()).isEqualTo(0);
            MemberThumbnail comment2Owner = comment2.owner();
            softAssertions.assertThat(comment2Owner.id()).isEqualTo(atom.getId());
            softAssertions.assertThat(comment2Owner.name()).isEqualTo(atom.getName());
            softAssertions.assertThat(comment2Owner.profileImage()).isEqualTo(atom.getProfileImageUrl());
            softAssertions.assertThat(comment2Owner.github()).isEqualTo(atom.getGithubUrl());
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
        member.setRefreshToken(UUID.randomUUID().toString());

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

    private CommentLike createCommentLike(Member member, Comment comment) {
        CommentLike commentLike = new CommentLike(member, comment);

        return commentLikeRepository.save(commentLike);
    }
}
