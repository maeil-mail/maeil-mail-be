package maeilwiki.mutiplechoice.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MultipleChoiceWorkbookTest {

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("객관식 문제집의 제목은 필수로 입력해야한다.")
    void validateTitleLength(String source) {
        assertThatThrownBy(() -> createWorkBook(source, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("객관식 문제집의 제목은 필수 입력값입니다.");
    }

    @Test
    @DisplayName("객관식 문제집 제목의 최대 길이는 255자이다.")
    void validateTitleMaxLength() {
        String invalidTitle = "*".repeat(256);

        assertThatThrownBy(() -> createWorkBook(invalidTitle, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("객관식 문제집의 제목은 255자 이하여야 합니다.");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6})
    @DisplayName("객관식 문제집의 난이도는 1 이상 5이하이다.")
    void validateDifficultyLevelRange(int source) {
        assertThatThrownBy(() -> createWorkBook("title", source))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("객관식 문제집의 난이도는 1 ~ 5 사이의 값으로 설정해주세요.");
    }

    private void createWorkBook(String title, int difficultyLevel) {
        new MultipleChoiceWorkbook(title, difficultyLevel, "BACKEND", "detail", 5);
    }
}
