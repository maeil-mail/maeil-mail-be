package maeilmail.bulksend.schedule;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.bulksend.schedule.helper.ResendDailyHelper;
import maeilmail.bulksend.schedule.helper.ResendWeeklyHelper;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import maeilmail.subscribe.command.domain.SubscribeQuestionRepository;
import maeilmail.support.DistributedSupport;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class ResendQuestionScheduler {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final DistributedSupport distributedSupport;
    private final SubscribeQuestionRepository subscribeQuestionRepository;
    private final ResendDailyHelper resendDailyHelper;
    private final ResendWeeklyHelper resendWeeklyHelper;

    @Scheduled(cron = "0 25 7 * * MON-FRI", zone = "Asia/Seoul")
    public void resendMail() {
        List<SubscribeQuestion> failed = getFailedSubscribeQuestions();
        if (failed.isEmpty()) {
            log.info("재전송 대상이 존재하지 않습니다.");
            return;
        }

        log.info("임시 재전송 로직을 수행합니다. 총 {}건의 실패 질문지(subscribeQuestion)가 존재합니다.", failed.size());
        Map<Long, List<SubscribeQuestion>> bundle = bundleQuestions(failed);
        removeFailedData(bundle);
        resendFailedMail(bundle);
    }

    private List<SubscribeQuestion> getFailedSubscribeQuestions() {
        LocalDateTime baseDate = ZonedDateTime.now(KOREA_ZONE).toLocalDate().atStartOfDay();

        return subscribeQuestionRepository.findAllFailedSubscribeQuestions(baseDate);
    }

    /**
     * 특정 subscribe의 질문지 맵(list 사이즈가 1인 경우 daily, 5인 경우 weekly)
     * 7시 0분에 전송 실패했지만, 추후에 재전송합니다. 이 기간 동안 구독자가 전송 주기를 변경하는 예외 케이스를 대응합니다
     */
    private Map<Long, List<SubscribeQuestion>> bundleQuestions(List<SubscribeQuestion> failed) {
        return failed.stream()
                .filter(this::isMine)
                .collect(Collectors.groupingBy(it -> it.getSubscribe().getId()));
    }

    private boolean isMine(SubscribeQuestion subscribeQuestion) {
        Subscribe subscribe = subscribeQuestion.getSubscribe();

        return distributedSupport.isMine(subscribe.getId());
    }

    private void removeFailedData(Map<Long, List<SubscribeQuestion>> bundle) {
        List<Long> removeTargetIds = bundle.values().stream()
                .flatMap(List::stream)
                .map(SubscribeQuestion::getId)
                .toList();

        subscribeQuestionRepository.removeAllByIdIn(removeTargetIds);
    }

    private void resendFailedMail(Map<Long, List<SubscribeQuestion>> bundle) {
        List<SubscribeQuestion> dailyTargets = extractDailyResendTargets(bundle);
        List<List<SubscribeQuestion>> weeklyTargets = extractWeeklyResendTargets(bundle);

        log.info("{}명의 일간 구독자와 {}명의 주간 구독자에게 질문지를 재전송합니다.", dailyTargets.size(), weeklyTargets.size());
        resendDailyHelper.resend(dailyTargets);
        resendWeeklyHelper.resend(weeklyTargets);
    }

    private List<SubscribeQuestion> extractDailyResendTargets(Map<Long, List<SubscribeQuestion>> bundle) {
        return bundle.values().stream()
                .filter(subscribeQuestions -> subscribeQuestions.size() == 1)
                .flatMap(List::stream)
                .toList();
    }

    private List<List<SubscribeQuestion>> extractWeeklyResendTargets(Map<Long, List<SubscribeQuestion>> bundle) {
        return bundle.values().stream()
                .filter(subscribeQuestions -> subscribeQuestions.size() == 5)
                .toList();
    }
}
