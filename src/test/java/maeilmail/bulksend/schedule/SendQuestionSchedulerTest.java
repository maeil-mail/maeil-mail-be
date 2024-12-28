package maeilmail.bulksend.schedule;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionRepository;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import maeilmail.support.IntegrationTestSupport;
import maeilmail.support.QueryCountTester;
import maeilmail.support.SchedulerTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SendQuestionSchedulerTest extends IntegrationTestSupport {

    @Autowired
    private SendQuestionScheduler sendQuestionScheduler;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private QueryCountTester queryCountTester;

    @Test
    @DisplayName("매주 월요일부터 금요일까지 평일에 한해서 아침 7시에 질문지 전송 스케줄러가 동작하는지 확인한다.")
    void sendMailCronWeekday() {
        LocalDateTime initialTime = LocalDateTime.of(2024, 8, 26, 7, 0); // 월요일
        List<LocalDateTime> expectedTimes = List.of(
                LocalDateTime.of(2024, 8, 27, 7, 0),  // 화요일
                LocalDateTime.of(2024, 8, 28, 7, 0),  // 수요일
                LocalDateTime.of(2024, 8, 29, 7, 0),  // 목요일
                LocalDateTime.of(2024, 8, 30, 7, 0),  // 금요일
                LocalDateTime.of(2024, 9, 2, 7, 0),   // 다음 주 월요일
                LocalDateTime.of(2024, 9, 3, 7, 0)    // 다음 주 화요일
        );
        SchedulerTestUtils.assertCronExpression(
                SendQuestionScheduler.class,
                "sendMail",
                toInstant(initialTime),
                expectedTimes.stream().map(this::toInstant).toList()
        );
    }

    @Test
    @DisplayName("오전 7시에 메일을 전송할때 캐시를 사용한다.")
    void sendMailCache() {
        // 4건
        for (int i = 0; i < 2; i++) {
            createQuestion(QuestionCategory.BACKEND);
            createQuestion(QuestionCategory.FRONTEND);
        }

        // 4건
        for (int i = 0; i < 2; i++) {
            createSubscribe(QuestionCategory.BACKEND);
            createSubscribe(QuestionCategory.FRONTEND);
        }

        // sendMail = 3건
        sendQuestionScheduler.sendMail();

        queryCountTester.assertQueryCount(11);
    }

    private Instant toInstant(LocalDateTime time) {
        return time.atZone(ZoneId.of("Asia/Seoul")).toInstant();
    }

    private Question createQuestion(QuestionCategory category) {
        Question question = new Question("test", "test", category);

        return questionRepository.save(question);
    }

    private Subscribe createSubscribe(QuestionCategory category) {
        Subscribe subscribe = new Subscribe("email@test.com", category, SubscribeFrequency.DAILY);

        return subscribeRepository.save(subscribe);
    }
}
