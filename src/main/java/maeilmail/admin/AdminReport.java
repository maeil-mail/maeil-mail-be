package maeilmail.admin;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import maeilmail.mail.MailEvent;

class AdminReport {

    private static final String REPORT_FORMAT = "질문 전송 카운트(타입/성공/실패) : %s/%d/%d";

    private final List<MailEvent> events;

    public AdminReport(List<MailEvent> events) {
        this.events = events;
    }

    public String generateReport(String type) {
        Map<Boolean, Long> result = events.stream()
                .filter(it -> it.getType().equals(type))
                .collect(Collectors.partitioningBy(MailEvent::isSuccess, Collectors.counting()));

        return String.format(REPORT_FORMAT, type, result.get(true), result.get(false));
    }
}
