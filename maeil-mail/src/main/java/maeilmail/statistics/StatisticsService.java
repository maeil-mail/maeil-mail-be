package maeilmail.statistics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import maeilmail.bulksend.schedule.SendWeeklyQuestionScheduler;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import maeilsupport.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final SubscribeRepository subscribeRepository;
    private final StatisticsDao statisticsDao;

    /**
     * 일간 전송 통계
     */
    public DailySendReport generateDailySendReport(LocalDate date) {
        LocalDateTime mailSendingTime = LocalDateTime.of(date, LocalTime.of(7, 0, 0));
        Map<SubscribeFrequency, Long> subscribeCountForFrequency = statisticsDao.querySubscribeCountForFrequency(mailSendingTime);
        Map<Boolean, Long> successFailCount = statisticsDao.querySuccessFailCount(mailSendingTime);

        return DailySendReport.generateDailySendReport(
                subscribeCountForFrequency,
                successFailCount,
                getFrequencyCountPolicy(date)
        );
    }

    private BiFunction<SubscribeFrequency, Long, Long> getFrequencyCountPolicy(LocalDate date) {
        return (subscribeFrequency, count) -> {
            if (DateUtils.isMonday(date)) {
                return count * SendWeeklyQuestionScheduler.WEEKLY_MAIL_SEND_COUNT;
            }

            return 0L;
        };
    }

    /**
     * 메인 페이지에서 사용되는 구독자 수
     */
    public SubscribeReport generateSubscribeReport() {
        List<String> distinctEmails = subscribeRepository.findDistinctEmails();

        return new SubscribeReport((long) distinctEmails.size());
    }

    /**
     * 특정 날짜 신규 구독자 수
     */
    public int countNewSubscribersOnSpecificDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        List<String> distinctEmails = subscribeRepository.findDistinctEmailsByCreatedAtBetween(startOfDay, endOfDay);

        return distinctEmails.size();
    }
}
