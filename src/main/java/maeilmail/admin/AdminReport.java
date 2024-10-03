package maeilmail.admin;

import java.util.List;
import maeilmail.subscribe.MailEvent;

class AdminReport {

    private static final String REPORT_FORMAT = "질문 전송 카운트(타입/성공/실패) : %s/%d/%d";

    private final List<MailEvent> events;

    public AdminReport(List<MailEvent> events) {
        this.events = events;
    }

    public String generateReport(String type) {
        return String.format(REPORT_FORMAT, type, calculateSuccessCount(type), calculateFailCount(type));
    }

    private int calculateSuccessCount(String type) {
        return (int) events.stream()
                .filter(it -> it.getType().equals(type))
                .filter(MailEvent::isSuccess)
                .count();
    }

    private int calculateFailCount(String type) {
        return (int) events.stream()
                .filter(it -> it.getType().equals(type))
                .filter(it -> !it.isSuccess())
                .count();
    }
}
