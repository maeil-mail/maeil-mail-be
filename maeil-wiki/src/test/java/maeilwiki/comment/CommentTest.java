package maeilwiki.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import maeilwiki.member.Member;
import maeilwiki.wiki.Wiki;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CommentTest {

    @Test
    @DisplayName("답변을 삭제할 수 있다.")
    void remove() {
        Comment comment = createComment();

        comment.remove();

        assertThat(comment.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 삭제된 답변은 다시 삭제될 수 없다.")
    void alreadyRemoved() {
        Comment comment = createComment();
        comment.remove();

        assertThatThrownBy(comment::remove)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 삭제된 답변입니다.");
    }

    private Comment createComment() {
        Member member = Mockito.mock(Member.class);
        Wiki wiki = Mockito.mock(Wiki.class);

        return new Comment("answer", false, member, wiki);
    }
}
