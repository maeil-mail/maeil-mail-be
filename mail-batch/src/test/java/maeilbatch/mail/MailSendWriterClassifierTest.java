package maeilbatch.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

import java.util.List;
import maeilbatch.mail.daily.DailyMailMessage;
import maeilbatch.mail.weekly.WeeklyMailMessage;
import maeilmail.mail.MailMessage;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ItemWriter;

class MailSendWriterClassifierTest {

    @Test
    @DisplayName("메일 메시지 타입에 따라 적절한 writer를 분기한다.")
    void classify() {
        ItemWriter<MailMessage> dailyWriter = createMockWriter();
        ItemWriter<MailMessage> weeklyWriter = createMockWriter();
        MailSendWriterClassifier classifier = new MailSendWriterClassifier(dailyWriter, weeklyWriter);
        DailyMailMessage dailyMessage = createDailyMessage();
        WeeklyMailMessage weeklyMessage = createWeeklyMessage();

        ItemWriter<? super MailMessage> dailyResult = classifier.classify(dailyMessage);
        ItemWriter<? super MailMessage> weeklyResult = classifier.classify(weeklyMessage);

        assertAll(
                () -> assertThat(dailyResult).isSameAs(dailyWriter),
                () -> assertThat(weeklyResult).isSameAs(weeklyWriter)
        );
    }

    @SuppressWarnings("unchecked")
    private ItemWriter<MailMessage> createMockWriter() {
        return mock(ItemWriter.class);
    }

    private DailyMailMessage createDailyMessage() {
        Subscribe subscribe = new Subscribe("daily@test.com", QuestionCategory.BACKEND, SubscribeFrequency.DAILY);
        Question question = new Question(1L, "title", "content", QuestionCategory.BACKEND);

        return new DailyMailMessage(subscribe, question, "subject", "text");
    }

    private WeeklyMailMessage createWeeklyMessage() {
        Subscribe subscribe = new Subscribe("weekly@test.com", QuestionCategory.BACKEND, SubscribeFrequency.WEEKLY);
        List<Question> questions = List.of(
                new Question(1L, "title-1", "content-1", QuestionCategory.BACKEND),
                new Question(2L, "title-2", "content-2", QuestionCategory.BACKEND)
        );

        return new WeeklyMailMessage(subscribe, questions, "subject", "text");
    }
}
