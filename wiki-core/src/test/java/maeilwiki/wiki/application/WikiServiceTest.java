package maeilwiki.wiki.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import maeilwiki.PaginationResponse;
import maeilwiki.comment.application.CommentRequest;
import maeilwiki.comment.application.CommentResponse;
import maeilwiki.comment.domain.Comment;
import maeilwiki.comment.domain.CommentLike;
import maeilwiki.comment.domain.CommentLikeRepository;
import maeilwiki.comment.domain.CommentRepository;
import maeilwiki.member.application.MemberIdentity;
import maeilwiki.member.domain.Member;
import maeilwiki.member.domain.MemberRepository;
import maeilwiki.member.dto.MemberThumbnail;
import maeilwiki.support.IntegrationTestSupport;
import maeilwiki.wiki.domain.Wiki;
import maeilwiki.wiki.domain.WikiRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

class WikiServiceTest extends IntegrationTestSupport {

    @Autowired
    private WikiRepository wikiRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private WikiService wikiService;

    @Test
    @DisplayName("존재하지 않는 위키에 답변을 작성할 수 없다.")
    void notfound() {
        CommentRequest request = new CommentRequest("답변을 작성합니다.", false);
        Long unknownWikiId = -1L;
        MemberIdentity identity = new MemberIdentity(1L, "name", "profileImage");

        assertThatThrownBy(() -> wikiService.comment(identity, request, unknownWikiId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("답변이 존재하는 위키는 삭제할 수 없다.")
    void cantRemove() {
        Member member = createMember();
        Wiki wiki = createWiki(member);
        Comment comment = createComment(member, wiki);
        MemberIdentity identity = new MemberIdentity(member.getId(), "name", "profileImage");

        assertThatThrownBy(() -> wikiService.remove(identity, wiki.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("답변이 존재하는 위키는 삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 위키는 삭제할 수 없다.")
    void cantRemoveUnknownWiki() {
        Member member = createMember();
        MemberIdentity identity = new MemberIdentity(member.getId(), "name", "profileImage");
        Long unknownWikiId = -1L;

        assertThatThrownBy(() -> wikiService.remove(identity, unknownWikiId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("자신의 위키만 삭제할 수 있다.")
    void cantRemoveOtherWiki() {
        Member member = createMember();
        Wiki wiki = createWiki(member);
        Member otherMember = createMember();
        MemberIdentity otherMemberIdentity = new MemberIdentity(otherMember.getId(), "name", "profileImage");

        assertThatThrownBy(() -> wikiService.remove(otherMemberIdentity, wiki.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("자신의 위키만 삭제할 수 있습니다.");
    }

    @Test
    @DisplayName("Wiki 아이디로 Wiki 단건을 조회한다.")
    void getWikiById() {
        // given
        Member prin = createMember();
        Member atom = createMember();
        Member leesang = createMember();
        MemberIdentity identity = new MemberIdentity(prin.getId(), "prin", "profileImage");

        Wiki wiki1 = createWiki(prin);
        Comment wiki1Comment1 = createComment(atom, wiki1);
        createCommentLike(leesang, wiki1Comment1);
        createCommentLike(prin, wiki1Comment1);
        Comment wiki1Comment2 = createComment(prin, wiki1);
        createCommentLike(atom, wiki1Comment2);
        Comment wiki1Comment3 = createComment(leesang, wiki1);


        Wiki wiki2 = createWiki(prin);
        Comment wiki2Comment1 = createComment(atom, wiki2);
        createCommentLike(prin, wiki2Comment1);
        createComment(atom, wiki2);

        // when
        WikiResponse wikiResponse = wikiService.getWikiById(identity, wiki1.getId());

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(wikiResponse.id()).isEqualTo(wiki1.getId());
            softAssertions.assertThat(wikiResponse.question()).isEqualTo(wiki1.getQuestion());
            softAssertions.assertThat(wikiResponse.questionDetail()).isEqualTo(wiki1.getQuestionDetail());
            softAssertions.assertThat(wikiResponse.category()).isEqualTo(wiki1.getCategory().toString().toLowerCase());
            MemberThumbnail wikiOwner = wikiResponse.owner();
            softAssertions.assertThat(wikiOwner.name()).isEqualTo(wiki1.getMember().getName());
            softAssertions.assertThat(wikiOwner.profileImage()).isEqualTo(wiki1.getMember().getProfileImageUrl());
            softAssertions.assertThat(wikiOwner.github()).isEqualTo(wiki1.getMember().getGithubUrl());
            softAssertions.assertThat(wikiResponse.comments()).hasSize(3);
            CommentResponse commentResponse1 = wikiResponse.comments().get(0);
            softAssertions.assertThat(commentResponse1.id()).isEqualTo(wiki1Comment1.getId());
            softAssertions.assertThat(commentResponse1.answer()).isEqualTo(wiki1Comment1.getAnswer());
            softAssertions.assertThat(commentResponse1.isLiked()).isTrue();
            softAssertions.assertThat(commentResponse1.likeCount()).isEqualTo(2);
            softAssertions.assertThat(commentResponse1.owner().id()).isEqualTo(atom.getId());
            CommentResponse commentResponse2 = wikiResponse.comments().get(1);
            softAssertions.assertThat(commentResponse2.id()).isEqualTo(wiki1Comment2.getId());
            softAssertions.assertThat(commentResponse2.answer()).isEqualTo(wiki1Comment2.getAnswer());
            softAssertions.assertThat(commentResponse2.isLiked()).isFalse();
            softAssertions.assertThat(commentResponse2.likeCount()).isEqualTo(1);
            softAssertions.assertThat(commentResponse2.owner().id()).isEqualTo(prin.getId());
            CommentResponse commentResponse3 = wikiResponse.comments().get(2);
            softAssertions.assertThat(commentResponse3.id()).isEqualTo(wiki1Comment3.getId());
            softAssertions.assertThat(commentResponse3.answer()).isEqualTo(wiki1Comment3.getAnswer());
            softAssertions.assertThat(commentResponse3.isLiked()).isFalse();
            softAssertions.assertThat(commentResponse3.likeCount()).isEqualTo(0);
            softAssertions.assertThat(commentResponse3.owner().id()).isEqualTo(leesang.getId());
        });
    }

    @Test
    @DisplayName("로그인하지 않은 사용자가 Wiki를 조회하면 답변 좋아요 여부는 모두 false이다.")
    void getWikiByIdWithoutLogin() {
        // given
        Member prin = createMember();
        Member atom = createMember();

        Wiki wiki = createWiki(prin);
        Comment comment1 = createComment(atom, wiki);
        createCommentLike(prin, comment1);
        Comment comment2 = createComment(atom, wiki);

        MemberIdentity identity = null;

        // when
        WikiResponse wikiResponse = wikiService.getWikiById(identity, wiki.getId());

        // then
        assertSoftly(softAssertions -> {
            List<CommentResponse> comments = wikiResponse.comments();
            softAssertions.assertThat(comments.get(0).isLiked()).isFalse();
            softAssertions.assertThat(comments.get(1).isLiked()).isFalse();
        });
    }

    @Test
    @DisplayName("Wiki가 익명이면 Wiki 작성자의 MemberID만 노출한다.")
    void getAnonymousWikiById() {
        // given
        boolean isAnonymousWiki = true;
        Member prin = createMember();
        MemberIdentity identity = new MemberIdentity(prin.getId(), "prin", "profileImage");
        Wiki wiki = createWiki(prin, isAnonymousWiki);
        createComment(prin, wiki);

        // when
        WikiResponse wikiResponse = wikiService.getWikiById(identity, wiki.getId());

        // then
        assertSoftly(softAssertions -> {
            MemberThumbnail owner = wikiResponse.owner();
            softAssertions.assertThat(owner.id()).isEqualTo(prin.getId());
            softAssertions.assertThat(owner.name()).isEqualTo(null);
            softAssertions.assertThat(owner.github()).isEqualTo(null);
            softAssertions.assertThat(owner.profileImage()).isEqualTo(null);
        });
    }

    @Test
    @DisplayName("Wiki가 익명이 아니라면 Wiki 작성자의 Member 정보를 노출한다.")
    void getNonAnonymousWikiById() {
        // given
        boolean isAnonymousWiki = false;
        Member prin = createMember();
        MemberIdentity identity = new MemberIdentity(prin.getId(), "prin", "profileImage");
        Wiki wiki = createWiki(prin, isAnonymousWiki);
        createComment(prin, wiki);

        // when
        WikiResponse wikiResponse = wikiService.getWikiById(identity, wiki.getId());

        // then
        assertSoftly(softAssertions -> {
            MemberThumbnail owner = wikiResponse.owner();
            softAssertions.assertThat(owner.id()).isEqualTo(prin.getId());
            softAssertions.assertThat(owner.name()).isEqualTo(prin.getName());
            softAssertions.assertThat(owner.github()).isEqualTo(prin.getGithubUrl());
            softAssertions.assertThat(owner.profileImage()).isEqualTo(prin.getProfileImageUrl());
        });
    }

    @Test
    @DisplayName("Wiki의 Comment가 익명이면 Comment 작성자의 MemberID만 노출한다.")
    void getWikiWithNonAnonymousCommentById() {
        // given
        boolean isAnonymousComment = true;
        Member atom = createMember();
        MemberIdentity identity = new MemberIdentity(atom.getId(), "atom", "profileImage");
        Wiki wiki = createWiki(atom);
        createComment(atom, wiki, isAnonymousComment);

        // when
        WikiResponse wikiResponse = wikiService.getWikiById(identity, wiki.getId());

        // then
        assertSoftly(softAssertions -> {
            MemberThumbnail owner = wikiResponse.comments().get(0).owner();
            softAssertions.assertThat(owner.id()).isEqualTo(atom.getId());
            softAssertions.assertThat(owner.name()).isEqualTo(null);
            softAssertions.assertThat(owner.github()).isEqualTo(null);
            softAssertions.assertThat(owner.profileImage()).isEqualTo(null);
        });
    }

    @Test
    @DisplayName("Wiki의 Comment가 익명이 아니라면, Comment 작성자의 Member 정보를 노출한다.")
    void getWikiWithAnonymousCommentById() {
        // given
        boolean isAnonymousComment = false;
        Member atom = createMember();
        MemberIdentity identity = new MemberIdentity(atom.getId(), "atom", "profileImage");
        Wiki wiki = createWiki(atom);
        createComment(atom, wiki, isAnonymousComment);

        // when
        WikiResponse wikiResponse = wikiService.getWikiById(identity, wiki.getId());

        // then
        assertSoftly(softAssertions -> {
            MemberThumbnail owner = wikiResponse.comments().get(0).owner();
            softAssertions.assertThat(owner.id()).isEqualTo(atom.getId());
            softAssertions.assertThat(owner.name()).isEqualTo(atom.getName());
            softAssertions.assertThat(owner.github()).isEqualTo(atom.getGithubUrl());
            softAssertions.assertThat(owner.profileImage()).isEqualTo(atom.getProfileImageUrl());
        });
    }

    @Test
    @DisplayName("카테고리에 해당하는 Wiki 페이지를 조회한다.")
    void pageByCategory() {
        // given
        Member prin = createMember();
        Member atom = createMember();
        Wiki wiki1 = createWiki(prin, "FRONTEND");
        Wiki wiki2 = createWiki(prin, "FRONTEND");
        createComment(atom, wiki1);
        createComment(atom, wiki1);
        createComment(atom, wiki1);
        createComment(atom, wiki2);
        createComment(atom, wiki2);

        // when
        PaginationResponse<WikiResponse> wikiResponses = wikiService.pageByCategory("FRONTEND", PageRequest.of(0, 3));

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(wikiResponses.isLastPage()).isTrue();
            softAssertions.assertThat(wikiResponses.totalPage()).isEqualTo(1);
            softAssertions.assertThat(wikiResponses.data()).hasSize(2);
            softAssertions.assertThat(wikiResponses.data().get(0).id()).isEqualTo(wiki2.getId());
            softAssertions.assertThat(wikiResponses.data().get(0).commentCount()).isEqualTo(2);
            softAssertions.assertThat(wikiResponses.data().get(1).id()).isEqualTo(wiki1.getId());
            softAssertions.assertThat(wikiResponses.data().get(1).commentCount()).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("각각의 Wiki마다 익명 여부에 따라 Wiki 작성자의 MemberID만 노출한다.")
    void pageByCategoryWithAnonymousWiki() {
        // given
        Member prin = createMember();
        Wiki anonymousWiki = createWiki(prin, true);
        Wiki nonanonymousWiki = createWiki(prin, false);

        // when
        PaginationResponse<WikiResponse> wikiResponses = wikiService.pageByCategory("all", PageRequest.of(0, 2));

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(wikiResponses.data().get(0).id()).isEqualTo(nonanonymousWiki.getId());

            MemberThumbnail nonAnonymousOwner = wikiResponses.data().get(0).owner();
            softAssertions.assertThat(nonAnonymousOwner.id()).isEqualTo(prin.getId());
            softAssertions.assertThat(nonAnonymousOwner.name()).isEqualTo(prin.getName());
            softAssertions.assertThat(nonAnonymousOwner.github()).isEqualTo(prin.getGithubUrl());
            softAssertions.assertThat(nonAnonymousOwner.profileImage()).isEqualTo(prin.getProfileImageUrl());

            MemberThumbnail anonymousOwner = wikiResponses.data().get(1).owner();
            softAssertions.assertThat(wikiResponses.data().get(1).id()).isEqualTo(anonymousWiki.getId());
            softAssertions.assertThat(anonymousOwner.id()).isEqualTo(prin.getId());
            softAssertions.assertThat(anonymousOwner.name()).isEqualTo(null);
            softAssertions.assertThat(anonymousOwner.github()).isEqualTo(null);
            softAssertions.assertThat(anonymousOwner.profileImage()).isEqualTo(null);
        });
    }

    private Member createMember() {
        Member member = new Member(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "GITHUB");
        member.setRefreshToken(UUID.randomUUID().toString());

        return memberRepository.save(member);
    }

    private Wiki createWiki(Member member) {
        return createWiki(member, "backend", false);
    }

    private Wiki createWiki(Member member, String category) {
        return createWiki(member, category, false);
    }

    private Wiki createWiki(Member member, boolean isAnonymous) {
        return createWiki(member, "backend", isAnonymous);
    }

    private Wiki createWiki(Member member, String category, boolean isAnonymous) {
        Wiki wiki = new Wiki("question", category, isAnonymous, member);

        return wikiRepository.save(wiki);
    }

    private Comment createComment(Member member, Wiki wiki) {
        return createComment(member, wiki, false);
    }

    private Comment createComment(Member member, Wiki wiki, boolean isAnonymous) {
        Comment comment = new Comment("answer", isAnonymous, member, wiki.getId());

        return commentRepository.save(comment);
    }

    private CommentLike createCommentLike(Member member, Comment comment) {
        CommentLike commentLike = new CommentLike(member, comment);

        return commentLikeRepository.save(commentLike);
    }
}
