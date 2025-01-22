package maeilwiki.wiki;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.Optional;
import maeilwiki.member.Member;
import maeilwiki.member.MemberRepository;
import maeilwiki.support.RepositoryTestSupport;
import maeilwiki.wiki.dto.WikiSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class WikiRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private WikiRepository wikiRepository;

    @Autowired
    private MemberRepository memberRepository;

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
            softAssertions.assertThat(wikiSummary.owner().name()).isEqualTo(wiki.getMember().getName());
            softAssertions.assertThat(wikiSummary.owner().profileImageUrl()).isEqualTo(wiki.getMember().getProfileImageUrl());
            softAssertions.assertThat(wikiSummary.owner().github()).isEqualTo(wiki.getMember().getGithubUrl());
            softAssertions.assertThat(wikiSummary.createdAt()).isEqualTo(wiki.getCreatedAt());
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
}
