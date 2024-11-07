package maeilmail.subscribe.core;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.DistributedSupport;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribeSequenceScheduler {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final SubscribeRepository subscribeRepository;
    private final DistributedSupport distributedSupport;

    @Transactional
    @Scheduled(cron = "0 0 22 * * MON-FRI", zone = "Asia/Seoul")
    public void increaseNextQuestionSequence() {
        if (distributedSupport.isMine(1L)) {
            LocalDateTime baseDateTime = ZonedDateTime.of(LocalDate.now(), LocalTime.of(7, 0), KOREA_ZONE).toLocalDateTime();
            subscribeRepository.increaseNextQuestionSequence(baseDateTime);
        }
    }
}
