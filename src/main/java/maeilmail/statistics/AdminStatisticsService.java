package maeilmail.statistics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilmail.subscribe.core.SubscribeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AdminStatisticsService {

    private final SubscribeRepository subscribeRepository;

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
}
