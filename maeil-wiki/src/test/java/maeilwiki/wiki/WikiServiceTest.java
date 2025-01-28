package maeilwiki.wiki;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import maeilwiki.comment.Comment;
import maeilwiki.comment.CommentRepository;
import maeilwiki.member.Member;
import maeilwiki.member.MemberRepository;
import maeilwiki.support.IntegrationTestSupport;
import maeilwiki.wiki.application.response.WikiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class WikiServiceTest extends IntegrationTestSupport {

    @Autowired
    private WikiService wikiService;

    @Autowired
    private WikiRepository wikiRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("Wiki 아이디로 Wiki 단건을 조회한다.")
    void getWikiById() {
        // given
        Member prin = memberRepository.save(new Member("prin", "UUID1", "GITHUB"));
        Member atom = memberRepository.save(new Member("atom", "UUID2", "GITHUB"));
        Wiki wiki1 = wikiRepository.save(new Wiki("질문1", "BACKEND", false, prin));
        Wiki wiki2 = wikiRepository.save(new Wiki("질문2", "FRONTEND", false, prin));
        commentRepository.save(new Comment("답변1", false, atom, wiki1));
        commentRepository.save(new Comment("답변2", false, atom, wiki2));
        commentRepository.save(new Comment("답변3", false, atom, wiki1));
        commentRepository.save(new Comment("답변4", false, atom, wiki2));

        // when
        WikiResponse wikiResponse = wikiService.getWikiById(wiki1.getId());

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(wikiResponse.id()).isEqualTo(wiki1.getId());
            softAssertions.assertThat(wikiResponse.question()).isEqualTo(wiki1.getQuestion());
            softAssertions.assertThat(wikiResponse.questionDetail()).isEqualTo(wiki1.getQuestionDetail());
            softAssertions.assertThat(wikiResponse.category()).isEqualTo(wiki1.getCategory().toString().toLowerCase());
            softAssertions.assertThat(wikiResponse.owner().name()).isEqualTo(wiki1.getMember().getName());
            softAssertions.assertThat(wikiResponse.owner().profileImageUrl()).isEqualTo(wiki1.getMember().getProfileImageUrl());
            softAssertions.assertThat(wikiResponse.owner().github()).isEqualTo(wiki1.getMember().getGithubUrl());
            softAssertions.assertThat(wikiResponse.createdAt()).isEqualTo(wiki1.getCreatedAt());
            softAssertions.assertThat(wikiResponse.comments()).hasSize(2);
            softAssertions.assertThat(wikiResponse.comments().get(0).answer()).isEqualTo("답변1");
            softAssertions.assertThat(wikiResponse.comments().get(1).answer()).isEqualTo("답변3");
        });
    }

    @Test
    @DisplayName("Wiki가 익명이면 Wiki 작성자를 null 처리한다.")
    void getAnonymousWikiById() {
        // given
        boolean isAnonymousWiki = true;
        Member prin = memberRepository.save(new Member("prin", "UUID", "GITHUB"));
        Wiki wiki = wikiRepository.save(new Wiki("질문1", "BACKEND", isAnonymousWiki, prin));
        commentRepository.save(new Comment("답변1", false, prin, wiki));

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
        Member atom = memberRepository.save(new Member("atom", "UUID1", "GITHUB"));
        Wiki wiki = wikiRepository.save(new Wiki("질문1", "BACKEND", false, atom));
        commentRepository.save(new Comment("답변1", isAnonymousComment, atom, wiki));

        // when
        WikiResponse wikiResponse = wikiService.getWikiById(wiki.getId());

        // then
        assertThat(wikiResponse.comments().get(0).owner()).isNull();
    }
}
