package maeilmail.statistics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilmail.subscribe.SubscribeRepository;
import maeilmail.subscribequestion.SubscribeQuestion;
import maeilmail.subscribequestion.SubscribeQuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final SubscribeRepository subscribeRepository;
    private final SubscribeQuestionRepository subscribeQuestionRepository;
    private final EventAggregator eventAggregator;

    public EventReport generateDailySubscribeQuestionReport() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusNanos(1);
        List<SubscribeQuestion> result = subscribeQuestionRepository.findSubscribeQuestionByCreatedAtBetween(startOfDay, endOfDay);

        return eventAggregator.aggregate(result);
    }

    public SubscribeReport generateSubscribeReport() {
        List<String> distinctEmails = subscribeRepository.findDistinctEmails();

        return new SubscribeReport((long) distinctEmails.size());
    }

    public int countNewSubscribersOnSpecificDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        List<String> distinctEmails = subscribeRepository.findDistinctEmailsByCreatedAtBetween(startOfDay, endOfDay);

        return distinctEmails.size();
    }
}
