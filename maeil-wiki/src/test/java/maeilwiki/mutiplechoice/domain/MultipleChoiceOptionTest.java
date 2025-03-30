package maeilwiki.mutiplechoice.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class MultipleChoiceOptionTest {

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("객관식 항목의 내용은 필수로 입력해야한다.")
    void validateContentLength(String source) {
        assertThatThrownBy(() -> new MultipleChoiceOption(source, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("객관식 항목의 내용은 필수 입력값입니다.");
    }

    @Test
    @DisplayName("객관식 항목 내용의 최대 길이는 255자이다.")
    void validateContentMaxLength() {
        String invalidContent = "*".repeat(256);

        assertThatThrownBy(() -> new MultipleChoiceOption(invalidContent, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("객관식 항목의 내용은 255자 이하여야 합니다.");
    }
}
