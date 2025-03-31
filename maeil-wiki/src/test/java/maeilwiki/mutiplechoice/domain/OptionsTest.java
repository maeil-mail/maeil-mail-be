package maeilwiki.mutiplechoice.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
}
