package maeilbatch.mail;

import java.util.Map;
import lombok.Builder;
import maeilmail.mail.MailView;
import maeilmail.mail.MailViewRenderer;
import maeilmail.statistics.DailySendReport;

@Builder
public class MailSendJobReportView implements MailView {

    private static final String REPORT_FORMAT = "질문 전송 카운트(전송 대상 건수/실제 전송 대상 건수/성공/실패) : %d/%d/%d/%d";

    private final MailViewRenderer renderer;
    private final DailySendReport dailySendReport;

    @Override
    public String render() {
        String reportText = String.format(
                REPORT_FORMAT,
                dailySendReport.expectedSendingCount(),
                dailySendReport.actualSendingCount(),
                dailySendReport.success(),
                dailySendReport.fail()
        );

        return renderer.render(Map.of("report", reportText), "report");
    }

    public String getType() {
        return "report";
    }
}
