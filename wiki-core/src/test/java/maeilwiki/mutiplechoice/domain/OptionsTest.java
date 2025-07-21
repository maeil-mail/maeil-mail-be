package maeilwiki.mutiplechoice.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OptionsTest {

    @Test
    @DisplayName("객관식 항목은 최소 1개 이상이어야 한다.")
    void validateSize() {
        List<Option> options = Collections.emptyList();

        assertThatThrownBy(() -> new Options(options))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("객관식 항목은 최소 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("적어도 정답인 객관식 항목은 최소 1개 이상이어야 한다.")
    void validateAnswerCount() {
        WorkbookQuestion question = mock(WorkbookQuestion.class);
        Option option1 = new Option("content", false, question);
        Option option2 = new Option("content", false, question);
        List<Option> options = List.of(option1, option2);

        assertThatThrownBy(() -> new Options(options))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("객관식 항목의 정답은 최소 1개 이상이어야 합니다.");
    }
}
