package maeilwiki.comment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;
import maeilwiki.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CommentServiceTest extends IntegrationTestSupport {

    @Autowired
    private CommentService commentService;

    @Test
    @DisplayName("존재하지 않는 위키에 답변을 작성할 수 없다.")
    void notfound() {
        CommentRequest request = new CommentRequest("답변을 작성합니다.", false);
        Long unknownWikiId = -1L;

        assertThatThrownBy(() -> commentService.comment(request, unknownWikiId))
                .isInstanceOf(NoSuchElementException.class);
    }
}
