package maeilwiki.wiki;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.Optional;
import maeilwiki.comment.Comment;
import maeilwiki.comment.CommentRepository;
import maeilwiki.member.Member;
import maeilwiki.member.MemberRepository;
import maeilwiki.support.IntegrationTestSupport;
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
        Member prin = memberRepository.save(new Member("prin", "UUID", "GITHUB"));
        Wiki wiki = wikiRepository.save(new Wiki("질문1", "FRONTEND", false, prin));

        // when
        WikiSummary wikiSummary = wikiRepository.queryOneById(wiki.getId()).orElseThrow();

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(wikiSummary.id()).isEqualTo(wiki.getId());
            softAssertions.assertThat(wikiSummary.question()).isEqualTo(wiki.getQuestion());
            softAssertions.assertThat(wikiSummary.questionDetail()).isEqualTo(wiki.getQuestionDetail());
            softAssertions.assertThat(wikiSummary.category()).isEqualTo(wiki.getCategory().toString().toLowerCase());
            softAssertions.assertThat(wikiSummary.isAnonymous()).isEqualTo(wiki.isAnonymous());
            softAssertions.assertThat(wikiSummary.createdAt()).isEqualTo(wiki.getCreatedAt());
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
        Member prin = memberRepository.save(new Member("prin", "UUID", "GITHUB"));
        Member atom = memberRepository.save(new Member("atom", "UUID2", "GITHUB"));
        Wiki wiki1 = wikiRepository.save(new Wiki("질문1", "FRONTEND", false, prin));
        Wiki wiki2 = wikiRepository.save(new Wiki("질문2", "FRONTEND", false, prin));
        wikiRepository.save(new Wiki("질문3", "BACKEND", false, prin));
        wikiRepository.save(new Wiki("질문4", "FRONTEND", false, prin));
        wikiRepository.save(new Wiki("질문5", "FRONTEND", false, prin));
        commentRepository.save(new Comment("답변1", true, atom, wiki1));
        commentRepository.save(new Comment("답변2", true, atom, wiki1));
        commentRepository.save(new Comment("답변3", true, atom, wiki1));
        commentRepository.save(new Comment("답변4", true, atom, wiki1));
        commentRepository.save(new Comment("답변5", true, atom, wiki2));
        commentRepository.save(new Comment("답변6", true, atom, wiki2));
        commentRepository.save(new Comment("답변7", true, atom, wiki2));
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
        Member prin = memberRepository.save(new Member("prin", "UUID", "GITHUB"));
        Member atom = memberRepository.save(new Member("atom", "UUID2", "GITHUB"));
        Wiki wiki1 = wikiRepository.save(new Wiki("질문1", "FRONTEND", false, prin));
        Wiki wiki2 = wikiRepository.save(new Wiki("질문2", "FRONTEND", false, prin));
        Wiki wiki3 = wikiRepository.save(new Wiki("질문3", "BACKEND", false, prin));
        Wiki wiki4 = wikiRepository.save(new Wiki("질문4", "FRONTEND", false, prin));
        Wiki wiki5 = wikiRepository.save(new Wiki("질문5", "FRONTEND", false, prin));
        commentRepository.save(new Comment("답변1", true, atom, wiki1));
        commentRepository.save(new Comment("답변2", true, atom, wiki1));
        commentRepository.save(new Comment("답변3", true, atom, wiki2));
        commentRepository.save(new Comment("답변4", true, atom, wiki3));
        commentRepository.save(new Comment("답변5", true, atom, wiki3));
        commentRepository.save(new Comment("답변6", true, atom, wiki3));
        commentRepository.save(new Comment("답변7", true, atom, wiki4));
        commentRepository.save(new Comment("답변8", true, atom, wiki4));
        commentRepository.save(new Comment("답변9", true, atom, wiki5));
        commentRepository.save(new Comment("답변10", true, atom, wiki5));
        commentRepository.save(new Comment("답변11", true, atom, wiki5));
        commentRepository.save(new Comment("답변12", true, atom, wiki5));
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
}
