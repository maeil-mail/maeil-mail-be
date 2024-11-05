package maeilmail.statistics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilmail.mail.MailEvent;
import maeilmail.mail.MailEventRepository;
import maeilmail.subscribe.core.SubscribeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final SubscribeRepository subscribeRepository;
    private final MailEventRepository mailEventRepository;
    private final MailEventAggregator mailEventAggregator;

    public int countCumulativeSubscribers() {
        List<String> distinctEmails = subscribeRepository.findDistinctEmails();

        return distinctEmails.size();
    }

    public int countNewSubscribersOnSpecificDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        List<String> distinctEmails = subscribeRepository.findDistinctEmailsByCreatedAtBetween(startOfDay, endOfDay);

        return distinctEmails.size();
    }

    public MailEventReport generateDailyMailEventReport(String type) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusNanos(1);
        List<MailEvent> result = mailEventRepository.findMailEventByCreatedAtBetween(startOfDay, endOfDay);

        return mailEventAggregator.generateReport(type, result);
    }
}