package maeilmail.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribequestion.SubscribeQuestion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventAggregatorTest {

    @Test
    @DisplayName("메일 이벤트를 받으면 하루 결과로 변환한다.")
    void report() {
        EventAggregator eventAggregator = new EventAggregator();

        List<SubscribeQuestion> subscribeQuestions = List.of(
                createSubscribeQuestion(true),
                createSubscribeQuestion(true),
                createSubscribeQuestion(false),
                createSubscribeQuestion(true)
        );

        EventReport result = eventAggregator.aggregate(subscribeQuestions);

        assertThat(result.success()).isEqualTo(3);
        assertThat(result.fail()).isEqualTo(1);
    }

    private SubscribeQuestion createSubscribeQuestion(boolean isSuccess) {
        return new SubscribeQuestion(
                new Subscribe("test@gmail.com", QuestionCategory.BACKEND, SubscribeFrequency.DAILY),
                new Question("test-title", "test-content", QuestionCategory.BACKEND),
                isSuccess
        );
    }
}
