package maeilwiki.member.infra.github;

import static org.assertj.core.api.Assertions.assertThat;

import maeilwiki.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GithubMemberTest {

    @Test
    @DisplayName("사용자 이름이 존재하지 않으면, 계정 이름으로 대체한다.")
    void replaceName() {
        String expected = "atom";
        GithubMember githubMember = new GithubMember(1234L, null, expected, null);
        Member member = githubMember.toMember();

        assertThat(member.getName()).isEqualTo(expected);
    }

    @Test
    @DisplayName("깃헙 사용자의 프로바이더 식별자는 GH 프리픽스로 시작한다.")
    void prefixWithGh() {
        String expected = "atom";
        GithubMember githubMember = new GithubMember(1234L, null, expected, null);
        Member member = githubMember.toMember();

        assertThat(member.getProviderId()).isEqualTo("GH-1234");
    }
}
