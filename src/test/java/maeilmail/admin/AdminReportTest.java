package maeilmail.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import maeilmail.subscribe.MailEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdminReportTest {

    @Test
    @DisplayName("메일 이벤트를 받으면 하루 결과로 변환한다.")
    void report() {
        List<MailEvent> events = List.of(
                createMailEvent(true, "type"),
                createMailEvent(true, "none"),
                createMailEvent(false, "type"),
                createMailEvent(true, "type")
        );
        AdminReport adminReport = new AdminReport(events);

        String result = adminReport.generateReport("type");

        assertThat(result).isEqualTo("질문 전송 카운트(타입/성공/실패) : type/2/1");
    }

    private MailEvent createMailEvent(boolean isSuccess, String type) {
        return new MailEvent(1L, "sample@test.com", type, isSuccess, LocalDate.now());
    }
}
