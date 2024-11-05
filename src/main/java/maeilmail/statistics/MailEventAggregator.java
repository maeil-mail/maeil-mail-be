package maeilmail.statistics;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import maeilmail.mail.MailEvent;
import org.springframework.stereotype.Component;

@Component
class MailEventAggregator {

    public MailEventReport generateReport(String type, List<MailEvent> events) {
        Map<Boolean, Long> result = events.stream()
                .filter(it -> it.getType().startsWith(type))
                .collect(Collectors.partitioningBy(MailEvent::isSuccess, Collectors.counting()));

        return new MailEventReport(type, result.get(true), result.get(false));
    }
}
