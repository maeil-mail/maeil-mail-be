package maeilbatch.jobconfig;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilbatch.processor.DailySubscribeProcessor;
import maeilbatch.reader.SubscribeItemReaderGenerator;
import maeilbatch.writer.DailyMailSendWriter;
import maeilmail.bulksend.sender.SubscribeQuestionMessage;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
class DailyMailSendJobConfig {

    private static final int CHUNK_SIZE = 100;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job dailyMailSendJob(Step dailyMailSendStep) {
        return new JobBuilder("dailyMailSendJob", jobRepository)
                .start(dailyMailSendStep)
                .next(changeSequenceStep())
                .next(adminReportSendStep())
                .build();
    }

    @Bean
    public Step dailyMailSendStep(
            JpaCursorItemReader<Subscribe> dailySubscribeReader,
            DailySubscribeProcessor dailySubscribeProcessor,
            DailyMailSendWriter dailyMailSendWriter
    ) {
        return new StepBuilder("dailyMailSendStep", jobRepository)
                .<Subscribe, SubscribeQuestionMessage>chunk(CHUNK_SIZE, transactionManager)
                .reader(dailySubscribeReader)
                .processor(dailySubscribeProcessor)
                .writer(dailyMailSendWriter)
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<Subscribe> dailySubscribeReader(
            @Value("#{jobParameters['datetime']}") LocalDateTime dateTime,
            SubscribeItemReaderGenerator subscribeItemReaderGenerator
    ) {
        return subscribeItemReaderGenerator.generate(SubscribeFrequency.DAILY, dateTime);
    }

    @Bean
    public Step changeSequenceStep() {
        return new StepBuilder("changeSequenceStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("구독자 시퀀스 1씩 증가");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step adminReportSendStep() {
        return new StepBuilder("adminReportSendStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("오늘의 발송 결과 메일 전송!");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
