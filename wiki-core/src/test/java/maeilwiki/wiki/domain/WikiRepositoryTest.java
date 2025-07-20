package maeilwiki.wiki.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.Optional;
import java.util.UUID;
import maeilwiki.comment.domain.Comment;
import maeilwiki.comment.domain.CommentRepository;
import maeilwiki.support.IntegrationTestSupport;
import maeilwiki.member.domain.Member;
import maeilwiki.member.domain.MemberRepository;
import maeilwiki.wiki.dto.WikiSummary;
import maeilwiki.wiki.dto.WikiSummaryWithCommentCount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class WikiRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private WikiRepository wikiRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("Wiki 아이디로 WikiSummary를 조회한다.")
    void queryOneById() {
        // given
        Member prin = createMember();
        Wiki wiki = createWiki(prin);

        // when
        WikiSummary wikiSummary = wikiRepository.queryOneById(wiki.getId()).orElseThrow();

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(wikiSummary.id()).isEqualTo(wiki.getId());
            softAssertions.assertThat(wikiSummary.question()).isEqualTo(wiki.getQuestion());
            softAssertions.assertThat(wikiSummary.questionDetail()).isEqualTo(wiki.getQuestionDetail());
            softAssertions.assertThat(wikiSummary.category()).isEqualTo(wiki.getCategory().toString().toLowerCase());
            softAssertions.assertThat(wikiSummary.isAnonymous()).isEqualTo(wiki.isAnonymous());
            softAssertions.assertThat(wikiSummary.owner().name()).isEqualTo(wiki.getMember().getName());
            softAssertions.assertThat(wikiSummary.owner().profileImage()).isEqualTo(wiki.getMember().getProfileImageUrl());
            softAssertions.assertThat(wikiSummary.owner().github()).isEqualTo(wiki.getMember().getGithubUrl());
        });
    }

    @Test
    @DisplayName("존재하지 않은 Wiki 아이디로 조회하면 빈 Optional을 반환한다.")
    void queryOneByIdNotFound() {
        // given
        Long notFoundWikiId = 9999L;

        // when
        Optional<WikiSummary> wikiSummary = wikiRepository.queryOneById(notFoundWikiId);

        // then
        assertThat(wikiSummary).isEmpty();
    }

    @Test
    @DisplayName("카테고리에 해당하는 Wiki 페이지를 id 기준 내림차순으로 조회한다.")
    void pageByCategory() {
        // given
        Member prin = createMember();
        Member atom = createMember();
        Wiki wiki1 = createWiki(prin, "FRONTEND");
        createComment(atom, wiki1);
        createComment(atom, wiki1);
        createComment(atom, wiki1);
        createComment(atom, wiki1);
        Wiki wiki2 = createWiki(prin, "FRONTEND");
        createComment(atom, wiki2);
        createComment(atom, wiki2);
        createComment(atom, wiki2);
        createWiki(prin, "BACKEND");
        createWiki(prin, "FRONTEND");
        createWiki(prin, "FRONTEND");
        Pageable pageable = PageRequest.of(1, 2);

        // when
        Page<WikiSummaryWithCommentCount> wikiSummaryPage = wikiRepository.pageByCategory("FRONTEND", pageable);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(wikiSummaryPage.getTotalElements()).isEqualTo(4);
            softAssertions.assertThat(wikiSummaryPage.getTotalPages()).isEqualTo(2);
            softAssertions.assertThat(wikiSummaryPage.getSize()).isEqualTo(2);
            softAssertions.assertThat(wikiSummaryPage.getContent().get(0).wikiSummary().id()).isEqualTo(wiki2.getId());
            softAssertions.assertThat(wikiSummaryPage.getContent().get(0).commentCount()).isEqualTo(3);
            softAssertions.assertThat(wikiSummaryPage.getContent().get(1).wikiSummary().id()).isEqualTo(wiki1.getId());
            softAssertions.assertThat(wikiSummaryPage.getContent().get(1).commentCount()).isEqualTo(4);
        });
    }

    @Test
    @DisplayName("카테고리가 all이면 모든 카테고리의 Wiki 페이지를 조회한다.")
    void pageByDefaultCategory() {
        // given
        Member prin = createMember();
        Member atom = createMember();
        Wiki wiki1 = createWiki(prin, "FRONTEND");
        createComment(atom, wiki1);
        createComment(atom, wiki1);
        Wiki wiki2 = createWiki(prin, "FRONTEND");
        createComment(atom, wiki2);
        Wiki wiki3 = createWiki(prin, "BACKEND");
        createComment(atom, wiki3);
        createComment(atom, wiki3);
        createComment(atom, wiki3);
        Wiki wiki4 = createWiki(prin, "FRONTEND");
        createComment(atom, wiki4);
        createComment(atom, wiki4);
        Wiki wiki5 = createWiki(prin, "FRONTEND");
        createComment(atom, wiki5);
        createComment(atom, wiki5);
        createComment(atom, wiki5);
        createComment(atom, wiki5);
        Pageable pageable = PageRequest.of(0, 3);

        // when
        Page<WikiSummaryWithCommentCount> wikiSummaryPage = wikiRepository.pageByCategory("all", pageable);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(wikiSummaryPage.getTotalElements()).isEqualTo(5);
            softAssertions.assertThat(wikiSummaryPage.getTotalPages()).isEqualTo(2);
            softAssertions.assertThat(wikiSummaryPage.getSize()).isEqualTo(3);
            softAssertions.assertThat(wikiSummaryPage.getContent().get(0).wikiSummary().id()).isEqualTo(wiki5.getId());
            softAssertions.assertThat(wikiSummaryPage.getContent().get(0).commentCount()).isEqualTo(4);
            softAssertions.assertThat(wikiSummaryPage.getContent().get(1).wikiSummary().id()).isEqualTo(wiki4.getId());
            softAssertions.assertThat(wikiSummaryPage.getContent().get(1).commentCount()).isEqualTo(2);
            softAssertions.assertThat(wikiSummaryPage.getContent().get(2).wikiSummary().id()).isEqualTo(wiki3.getId());
            softAssertions.assertThat(wikiSummaryPage.getContent().get(2).commentCount()).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("삭제된 커멘트만 존재하는 위키도 조회되어야 한다.")
    void pageByCategoryWithOnlyDeletedCommend() {
        // given
        Member prin = createMember();
        Wiki wiki1 = createWiki(prin, "FRONTEND");
        createRemovedComment(prin, wiki1);
        createRemovedComment(prin, wiki1);

        Wiki wiki2 = createWiki(prin, "FRONTEND");
        createComment(prin, wiki2);
        createRemovedComment(prin, wiki2);

        Wiki wiki3 = createWiki(prin, "FRONTEND");

        Wiki wiki4 = createWiki(prin, "FRONTEND");
        createComment(prin, wiki4);
        createComment(prin, wiki4);

        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<WikiSummaryWithCommentCount> wikiSummaryPage = wikiRepository.pageByCategory("all", pageable);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(wikiSummaryPage.getTotalElements()).isEqualTo(4);
            softAssertions.assertThat(wikiSummaryPage.getTotalPages()).isEqualTo(1);
            softAssertions.assertThat(wikiSummaryPage.getSize()).isEqualTo(5);
            softAssertions.assertThat(wikiSummaryPage.getContent().get(0).wikiSummary().id()).isEqualTo(wiki4.getId());
            softAssertions.assertThat(wikiSummaryPage.getContent().get(0).commentCount()).isEqualTo(2);
            softAssertions.assertThat(wikiSummaryPage.getContent().get(1).wikiSummary().id()).isEqualTo(wiki3.getId());
            softAssertions.assertThat(wikiSummaryPage.getContent().get(1).commentCount()).isEqualTo(0);
            softAssertions.assertThat(wikiSummaryPage.getContent().get(2).wikiSummary().id()).isEqualTo(wiki2.getId());
            softAssertions.assertThat(wikiSummaryPage.getContent().get(2).commentCount()).isEqualTo(1);
            softAssertions.assertThat(wikiSummaryPage.getContent().get(3).wikiSummary().id()).isEqualTo(wiki1.getId());
            softAssertions.assertThat(wikiSummaryPage.getContent().get(3).commentCount()).isEqualTo(0);
        });
    }

    private Member createMember() {
        Member member = new Member(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "GITHUB");
        member.setRefreshToken(UUID.randomUUID().toString());

        return memberRepository.save(member);
    }

    private Wiki createWiki(Member member) {
        return createWiki(member, "backend");
    }

    private Wiki createWiki(Member member, String category) {
        Wiki wiki = new Wiki("question", category, false, member);

        return wikiRepository.save(wiki);
    }

    private Comment createComment(Member member, Wiki wiki) {
        Comment comment = new Comment("answer", false, member, wiki.getId());

        return commentRepository.save(comment);
    }

    private Comment createRemovedComment(Member member, Wiki wiki) {
        Comment comment = new Comment("answer", false, member, wiki.getId());
        comment.remove();
        return commentRepository.save(comment);
    }
}
