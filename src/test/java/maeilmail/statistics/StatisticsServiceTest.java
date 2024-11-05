package maeilmail.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import maeilmail.mail.MailEvent;
import maeilmail.mail.MailEventRepository;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.core.Subscribe;
import maeilmail.subscribe.core.SubscribeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class StatisticsServiceTest {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private MailEventRepository mailEventRepository;

    @Test
    @DisplayName("고유한 이메일을 가진 누적 구독자 수를 반환한다.")
    void countCumulativeSubscribers() {
        List<Subscribe> subscribes = IntStream.rangeClosed(1, 10)
                .mapToObj(index -> new Subscribe("test" + index, QuestionCategory.BACKEND))
                .toList();
        subscribeRepository.save(new Subscribe("test" + 1, QuestionCategory.FRONTEND));
        subscribeRepository.save(new Subscribe("test" + 2, QuestionCategory.FRONTEND));
        subscribeRepository.save(new Subscribe("test" + 3, QuestionCategory.FRONTEND));
        subscribeRepository.saveAll(subscribes);

        int distinctEmailsCount = statisticsService.countCumulativeSubscribers();

        assertThat(distinctEmailsCount).isEqualTo(10);
    }

    @Test
    @DisplayName("고유한 이메일을 가진 일별 신규 구독자 수를 반환한다.")
    void countNewSubscribersOnSpecificDate() {
        subscribeRepository.save(new Subscribe("test" + 1, QuestionCategory.FRONTEND));
        subscribeRepository.save(new Subscribe("test" + 1, QuestionCategory.BACKEND));
        subscribeRepository.save(new Subscribe("test" + 2, QuestionCategory.BACKEND));
        subscribeRepository.save(new Subscribe("test" + 2, QuestionCategory.FRONTEND));
        subscribeRepository.save(new Subscribe("test" + 2, QuestionCategory.FRONTEND));

        int distinctEmailsCount = statisticsService.countNewSubscribersOnSpecificDate(LocalDate.now());

        assertThat(distinctEmailsCount).isEqualTo(2);
    }

    @Test
    @DisplayName("해당 날짜에 구독한 신규 구독자가 없으면 0명을 반환한다.")
    void countNewSubscribersOnSpecificDate2() {
        subscribeRepository.save(new Subscribe("test" + 1, QuestionCategory.FRONTEND));

        int distinctEmailsCount = statisticsService.countNewSubscribersOnSpecificDate(LocalDate.now().minusDays(1));

        assertThat(distinctEmailsCount).isEqualTo(0);
    }

    @Test
    @DisplayName("주어진 타입의 오늘 발생한 메일 이벤트의 성공과 실패를 집계한다.")
    void generateDailyMailEventReport() {
        mailEventRepository.save(MailEvent.success("temp1@gmail.com", "question"));
        mailEventRepository.save(MailEvent.success("temp2@gmail.com", "question"));
        mailEventRepository.save(MailEvent.fail("temp3@gmail.com", "question"));

        MailEventReport mailEventReport = statisticsService.generateDailyMailEventReport("question");

        assertThat(mailEventReport.success()).isEqualTo(2);
        assertThat(mailEventReport.fail()).isEqualTo(1);
    }
}
