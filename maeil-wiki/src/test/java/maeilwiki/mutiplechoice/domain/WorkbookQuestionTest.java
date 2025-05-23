package maeilwiki.mutiplechoice.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class WorkbookQuestionTest {

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("객관식 문제의 제목은 필수로 입력해야한다.")
    void validateTitleLength(String source) {
        Workbook workbook = mock(Workbook.class);

        assertThatThrownBy(() -> new WorkbookQuestion(source, "explanation", workbook))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("객관식 문제의 제목은 필수 입력값입니다.");
    }

    @Test
    @DisplayName("객관식 문제 제목의 최대 길이는 255자이다.")
    void validateTitleMaxLength() {
        String invalidTitle = "*".repeat(256);
        Workbook workbook = mock(Workbook.class);

        assertThatThrownBy(() -> new WorkbookQuestion(invalidTitle, "explanation", workbook))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("객관식 문제의 제목은 255자 이하여야 합니다.");
    }
}
