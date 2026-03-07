package maeilbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.test.util.ReflectionTestUtils;

class ChangeSequenceTaskletTest {

    @Test
    @DisplayName("구독자 시퀀스 증가 쿼리를 실행하고 FINISHED를 반환한다.")
    void executeSuccess() {
        SubscribeRepository subscribeRepository = mock(SubscribeRepository.class);
        ChangeSequenceTasklet tasklet = new ChangeSequenceTasklet(subscribeRepository);
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 5, 1, 7, 0);
        ReflectionTestUtils.setField(tasklet, "dateTime", baseDateTime);

        RepeatStatus result = tasklet.execute(null, null);

        assertAll(
                () -> verify(subscribeRepository, times(1)).increaseNextQuestionSequence(baseDateTime),
                () -> assertThat(result).isEqualTo(RepeatStatus.FINISHED)
        );
    }

    @Test
    @DisplayName("시퀀스 증가 중 예외가 발생해도 FINISHED를 반환한다.")
    void executeFail() {
        SubscribeRepository subscribeRepository = mock(SubscribeRepository.class);
        ChangeSequenceTasklet tasklet = new ChangeSequenceTasklet(subscribeRepository);
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 5, 1, 7, 0);
        ReflectionTestUtils.setField(tasklet, "dateTime", baseDateTime);
        doThrow(new IllegalStateException("fail"))
                .when(subscribeRepository)
                .increaseNextQuestionSequence(baseDateTime);

        RepeatStatus result = tasklet.execute(null, null);

        assertAll(
                () -> verify(subscribeRepository, times(1)).increaseNextQuestionSequence(baseDateTime),
                () -> assertThat(result).isEqualTo(RepeatStatus.FINISHED)
        );
    }
}
