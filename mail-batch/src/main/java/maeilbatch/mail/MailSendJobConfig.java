package maeilbatch.mail;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilbatch.mail.daily.DailyMailSendWriter;
import maeilbatch.mail.daily.DailySubscribeProcessor;
import maeilmail.bulksend.sender.SubscribeQuestionMessage;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
class MailSendJobConfig {

    private static final int CHUNK_SIZE = 100;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job dailyMailSendJob(
            Step dailyMailSendStep,
            Step changeSequenceStep,
            JobExecutionListener mailSendJobReportListener
    ) {
        return new JobBuilder("mailSendJob", jobRepository)
                .start(dailyMailSendStep)
                .next(changeSequenceStep)
                .listener(mailSendJobReportListener)
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
    public Step changeSequenceStep(IncreaseSubscribeSequenceTasklet increaseSubscribeSequenceTasklet) {
        return new StepBuilder("dailyIncreaseSubscribeSequenceTasklet", jobRepository)
                .tasklet(increaseSubscribeSequenceTasklet, transactionManager)
                .build();
    }
}
