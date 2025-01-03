package maeilmail.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionRepository;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import maeilmail.subscribe.command.domain.SubscribeQuestionRepository;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import maeilmail.support.IntegrationTestSupport;
import maeilmail.support.data.SendReportCountingCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class StatisticsServiceTest extends IntegrationTestSupport {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private SubscribeQuestionRepository subscribeQuestionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SendReportCountingCase sendReportCountingCase;

    @Test
    @DisplayName("고유한 이메일을 가진 누적 구독자 수를 반환한다.")
    void generateSubscribeReport() {
        List<Subscribe> subscribes = IntStream.rangeClosed(1, 10)
                .mapToObj(index -> new Subscribe("test" + index, QuestionCategory.BACKEND, SubscribeFrequency.DAILY))
                .toList();
        subscribeRepository.save(new Subscribe("test" + 1, QuestionCategory.FRONTEND, SubscribeFrequency.DAILY));
        subscribeRepository.save(new Subscribe("test" + 2, QuestionCategory.FRONTEND, SubscribeFrequency.DAILY));
        subscribeRepository.save(new Subscribe("test" + 3, QuestionCategory.FRONTEND, SubscribeFrequency.DAILY));
        subscribeRepository.saveAll(subscribes);

        SubscribeReport subscribeReport = statisticsService.generateSubscribeReport();

        assertThat(subscribeReport.cumulativeCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("고유한 이메일을 가진 일별 신규 구독자 수를 반환한다.")
    void countNewSubscribersOnSpecificDate() {
        subscribeRepository.save(new Subscribe("test" + 1, QuestionCategory.FRONTEND, SubscribeFrequency.DAILY));
        subscribeRepository.save(new Subscribe("test" + 1, QuestionCategory.BACKEND, SubscribeFrequency.DAILY));
        subscribeRepository.save(new Subscribe("test" + 2, QuestionCategory.BACKEND, SubscribeFrequency.DAILY));
        subscribeRepository.save(new Subscribe("test" + 2, QuestionCategory.FRONTEND, SubscribeFrequency.DAILY));
        subscribeRepository.save(new Subscribe("test" + 2, QuestionCategory.FRONTEND, SubscribeFrequency.DAILY));

        int distinctEmailsCount = statisticsService.countNewSubscribersOnSpecificDate(LocalDate.now());

        assertThat(distinctEmailsCount).isEqualTo(2);
    }

    @Test
    @DisplayName("해당 날짜에 구독한 신규 구독자가 없으면 0명을 반환한다.")
    void countNewSubscribersOnSpecificDate2() {
        subscribeRepository.save(new Subscribe("test" + 1, QuestionCategory.FRONTEND, SubscribeFrequency.DAILY));

        int distinctEmailsCount = statisticsService.countNewSubscribersOnSpecificDate(LocalDate.now().minusDays(1));

        assertThat(distinctEmailsCount).isEqualTo(0);
    }

    @Test
    @DisplayName("주어진 타입의 오늘 발생한 메일 이벤트의 성공과 실패를 집계한다.")
    void generateDailySubscribeQuestionReport() {
        subscribeQuestionRepository.save(createSubscribeQuestion(true));
        subscribeQuestionRepository.save(createSubscribeQuestion(true));
        subscribeQuestionRepository.save(createSubscribeQuestion(false));

        EventReport eventReport = statisticsService.generateDailySubscribeQuestionReport();

        assertThat(eventReport.success()).isEqualTo(2);
        assertThat(eventReport.fail()).isEqualTo(1);
    }

    @Test
    @DisplayName("주어진 일자의 전송 통계를 생성한다.")
    void generateDailySendReport() {
        sendReportCountingCase.createData();

        LocalDate monday = LocalDate.of(2024, 12, 30);
        LocalDate tuesday = LocalDate.of(2024, 12, 31);

        DailySendReport actualMonday = statisticsService.generateDailySendReport(monday);
        DailySendReport actualTuesday = statisticsService.generateDailySendReport(tuesday);

        assertAll(
                () -> assertThat(actualMonday.expectedSendingCount()).isEqualTo(12L),
                () -> assertThat(actualMonday.actualSendingCount()).isEqualTo(7L),
                () -> assertThat(actualMonday.success()).isEqualTo(5L),
                () -> assertThat(actualMonday.fail()).isEqualTo(2L),
                () -> assertThat(actualTuesday.expectedSendingCount()).isEqualTo(8L),
                () -> assertThat(actualTuesday.actualSendingCount()).isEqualTo(8L),
                () -> assertThat(actualTuesday.success()).isEqualTo(8L),
                () -> assertThat(actualTuesday.fail()).isEqualTo(0L)
        );
    }

    private SubscribeQuestion createSubscribeQuestion(boolean isSuccess) {
        Subscribe subscribe = subscribeRepository.save(new Subscribe("test@gmail.com", QuestionCategory.BACKEND, SubscribeFrequency.DAILY));
        Question question = questionRepository.save(
                new Question(
                        "test-title",
                        "test-content",
                        QuestionCategory.BACKEND
                )
        );
        return new SubscribeQuestion(subscribe, question, isSuccess);
    }
}
