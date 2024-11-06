package maeilmail.subscribe.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import maeilmail.question.QuestionCategory;
import maeilmail.support.SchedulerTestUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SubscribeSequenceSchedulerTest {

    @Autowired
    private SubscribeSequenceScheduler subscribeSequenceScheduler;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Test
    @DisplayName("매주 월요일부터 금요일까지 평일에 한해서 저녁 10시에 시퀀스 증가 스케줄러가 동작하는지 확인한다.")
    void increaseNextQuestionSequenceCronWeekday() {
        LocalDateTime initialTime = LocalDateTime.of(2024, 11, 4, 22, 0); // 월요일
        List<LocalDateTime> expectedTimes = List.of(
                LocalDateTime.of(2024, 11, 5, 22, 0),   // 화요일
                LocalDateTime.of(2024, 11, 6, 22, 0),   // 수요일
                LocalDateTime.of(2024, 11, 7, 22, 0),   // 목요일
                LocalDateTime.of(2024, 11, 8, 22, 0),   // 금요일
                LocalDateTime.of(2024, 11, 11, 22, 0),  // 다음주 월요일
                LocalDateTime.of(2024, 11, 12, 22, 0)   // 다음주 화요일
        );
        SchedulerTestUtils.assertCronExpression(
                SubscribeSequenceScheduler.class,
                "increaseNextQuestionSequence",
                toInstant(initialTime),
                expectedTimes.stream().map(this::toInstant).toList()
        );
    }

    @Disabled
    @DisplayName("오전 7시 이전에 구독한 구독자의 질문지 시퀀스를 증가시킨다.")
    @Test
    void increaseNextQuestionSequence() {
        LocalDateTime baseDateTime = LocalDateTime.of(2024, 11, 6, 7, 0);
        Subscribe backend = new Subscribe("test@test.com", QuestionCategory.BACKEND);
        Subscribe frontend = new Subscribe("test@test.com", QuestionCategory.FRONTEND);
        Subscribe afterBaseDateTimeSubscriber = new Subscribe("after@test.com", QuestionCategory.FRONTEND);

        subscribeRepository.saveAll(List.of(backend, frontend, afterBaseDateTimeSubscriber));

        subscribeSequenceScheduler.increaseNextQuestionSequence();

        List<Subscribe> subscribes = subscribeRepository.findAll();

        assertThat(subscribes.get(0).getNextQuestionSequence()).isEqualTo(1);
        assertThat(subscribes.get(1).getNextQuestionSequence()).isEqualTo(1);
        assertThat(subscribes.get(2).getNextQuestionSequence()).isEqualTo(0);
    }

    private Instant toInstant(LocalDateTime time) {
        return time.atZone(ZoneId.of("Asia/Seoul")).toInstant();
    }
}
