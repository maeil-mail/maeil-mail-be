package maeilbatch.mail;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@StepScope
@Component
@RequiredArgsConstructor
public class IncreaseSubscribeSequenceTasklet implements Tasklet {

    private final SubscribeRepository subscribeRepository;

    @Value("#{jobParameters['datetime']}")
    private LocalDateTime baseDateTime;

    @Value("#{jobParameters['sendTarget']}")
    private SubscribeFrequency sendType;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            subscribeRepository.increasePartialNextQuestionSequence(baseDateTime, sendType.getSendCount());
        } catch (Exception e) {
            log.error("구독자 시퀀스 증가 실패 baseDatetime = {}, sendType = {}", baseDateTime, sendType.toLowerCase(), e);
        }

        return RepeatStatus.FINISHED;
    }
}
