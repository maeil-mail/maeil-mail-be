package maeilwiki.wiki;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import maeilwiki.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class WikiTest {

    @Test
    @DisplayName("위키를 삭제할 수 있다.")
    void remove() {
        Wiki wiki = createWiki();

        wiki.remove();

        assertThat(wiki.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 삭제된 위키는 다시 삭제될 수 없다.")
    void alreadyRemoved() {
        Wiki wiki = createWiki();
        wiki.remove();

        assertThatThrownBy(wiki::remove)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 삭제된 위키입니다.");
    }

    private Wiki createWiki() {
        Member member = Mockito.mock(Member.class);

        return new Wiki("question", "detail", "backend", false, member);
    }
}
