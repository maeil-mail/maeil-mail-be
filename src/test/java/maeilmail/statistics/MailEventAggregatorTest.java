package maeilmail.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import maeilmail.mail.MailEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MailEventAggregatorTest {

    @Test
    @DisplayName("메일 이벤트를 받으면 하루 결과로 변환한다.")
    void report() {
        MailEventAggregator mailEventAggregator = new MailEventAggregator();
        List<MailEvent> events = List.of(
                createMailEvent(true, "type"),
                createMailEvent(true, "none"),
                createMailEvent(false, "type"),
                createMailEvent(true, "type")
        );

        MailEventReport result = mailEventAggregator.generateReport("type", events);

        assertThat(result.success()).isEqualTo(2);
        assertThat(result.fail()).isEqualTo(1);
    }

    private MailEvent createMailEvent(boolean isSuccess, String type) {
        return new MailEvent(1L, "sample@test.com", type, isSuccess);
    }
}