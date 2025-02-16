package maeilwiki.wiki.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.NoSuchElementException;
import java.util.UUID;
import maeilsupport.PaginationResponse;
import maeilwiki.comment.application.CommentRequest;
import maeilwiki.comment.domain.Comment;
import maeilwiki.comment.domain.CommentRepository;
import maeilwiki.member.application.MemberIdentity;
import maeilwiki.member.domain.Member;
import maeilwiki.member.domain.MemberRepository;
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
        Wiki wiki1 = createWiki(prin);
        Wiki wiki2 = createWiki(prin);
        Comment wiki1Comment1 = createComment(atom, wiki1);
        createComment(atom, wiki2);
        Comment wiki1Comment2 = createComment(atom, wiki1);
        createComment(atom, wiki2);

        // when
        WikiResponse wikiResponse = wikiService.getWikiById(wiki1.getId());

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(wikiResponse.id()).isEqualTo(wiki1.getId());
            softAssertions.assertThat(wikiResponse.question()).isEqualTo(wiki1.getQuestion());
            softAssertions.assertThat(wikiResponse.questionDetail()).isEqualTo(wiki1.getQuestionDetail());
            softAssertions.assertThat(wikiResponse.category()).isEqualTo(wiki1.getCategory().toString().toLowerCase());
            softAssertions.assertThat(wikiResponse.owner().name()).isEqualTo(wiki1.getMember().getName());
            softAssertions.assertThat(wikiResponse.owner().profileImage()).isEqualTo(wiki1.getMember().getProfileImageUrl());
            softAssertions.assertThat(wikiResponse.owner().github()).isEqualTo(wiki1.getMember().getGithubUrl());
            softAssertions.assertThat(wikiResponse.comments()).hasSize(2);
            softAssertions.assertThat(wikiResponse.comments().get(0).id()).isEqualTo(wiki1Comment1.getId());
            softAssertions.assertThat(wikiResponse.comments().get(1).id()).isEqualTo(wiki1Comment2.getId());
        });
    }

    @Test
    @DisplayName("Wiki가 익명이면 Wiki 작성자를 null 처리한다.")
    void getAnonymousWikiById() {
        // given
        boolean isAnonymousWiki = true;
        Member prin = createMember();
        Wiki wiki = createWiki(prin, isAnonymousWiki);
        createComment(prin, wiki);

        // when
        WikiResponse wikiResponse = wikiService.getWikiById(wiki.getId());

        // then
        assertThat(wikiResponse.owner()).isNull();
    }

    @Test
    @DisplayName("Wiki의 Comment가 익명이면 Comment 작성자를 null 처리한다.")
    void getWikiWithAnonymousCommentById() {
        // given
        boolean isAnonymousComment = true;
        Member atom = createMember();
        Wiki wiki = createWiki(atom);
        createComment(atom, wiki, isAnonymousComment);

        // when
        WikiResponse wikiResponse = wikiService.getWikiById(wiki.getId());

        // then
        assertThat(wikiResponse.comments().get(0).owner()).isNull();
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
    @DisplayName("각각의 Wiki마다 익명 여부에 따라 Wiki 작성자를 null 처리한다.")
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
            softAssertions.assertThat(wikiResponses.data().get(0).owner()).isNotNull();
            softAssertions.assertThat(wikiResponses.data().get(1).id()).isEqualTo(anonymousWiki.getId());
            softAssertions.assertThat(wikiResponses.data().get(1).owner()).isNull();
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
}
