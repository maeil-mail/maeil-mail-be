package maeilbatch;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilbatch.forward.ForwardLog;
import maeilbatch.forward.ForwardProcessor;
import maeilbatch.forward.ForwardReader;
import maeilbatch.forward.ForwardWriter;
import maeilbatch.mail.AbstractMailPayload;
import maeilbatch.mail.FilterSubscribeProcessor;
import maeilbatch.mail.MailSendItemReader;
import maeilbatch.mail.MailSendPartitioner;
import maeilbatch.mail.MailSendProcessorClassifier;
import maeilbatch.mail.MailSendWriterClassifier;
import maeilmail.subscribe.command.domain.Subscribe;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.support.ClassifierCompositeItemProcessor;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
class MailSendJobConfig {

    private static final int CHUNK_SIZE = 100;
    private static final int POOL_SIZE = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job mailSendJob(
            Step mailGenerateStep,
            Step managerMailSendStep,
            Step changeSequenceStep,
            JobExecutionListener mailSendJobReportListener
    ) {
        return new JobBuilder("mailSendJob", jobRepository)
                .start(mailGenerateStep)
                .next(managerMailSendStep)
                .next(changeSequenceStep)
                .listener(mailSendJobReportListener)
                .build();
    }

    @Bean
    public Step mailGenerateStep(
            JdbcPagingItemReader<Subscribe> subscribeReader,
            CompositeItemProcessor<Subscribe, AbstractMailPayload> mailSendProcessor,
            ClassifierCompositeItemWriter<AbstractMailPayload> mailSendWriter
    ) {
        return new StepBuilder("mailGenerateStep", jobRepository)
                .<Subscribe, AbstractMailPayload>chunk(CHUNK_SIZE, transactionManager)
                .reader(subscribeReader)
                .processor(mailSendProcessor)
                .writer(mailSendWriter)
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<Subscribe> subscribeReader(
            @Value("#{jobParameters['datetime']}") LocalDateTime dateTime,
            MailSendItemReader mailSendItemReader
    ) {
        return mailSendItemReader.generate(dateTime);
    }

    @Bean
    public CompositeItemProcessor<Subscribe, AbstractMailPayload> mailSendProcessor(
            FilterSubscribeProcessor filterSubscribeProcessor,
            ClassifierCompositeItemProcessor<Subscribe, AbstractMailPayload> mailPayloadProcessor
    ) {
        CompositeItemProcessor<Subscribe, AbstractMailPayload> mailSendProcessor = new CompositeItemProcessor<>();
        mailSendProcessor.setDelegates(List.of(filterSubscribeProcessor, mailPayloadProcessor));

        return mailSendProcessor;
    }

    @Bean
    public ClassifierCompositeItemProcessor<Subscribe, AbstractMailPayload> mailPayloadProcessor(
            MailSendProcessorClassifier classifier
    ) {
        ClassifierCompositeItemProcessor<Subscribe, AbstractMailPayload> processor = new ClassifierCompositeItemProcessor<>();
        processor.setClassifier(classifier);

        return processor;
    }

    @Bean
    public ClassifierCompositeItemWriter<AbstractMailPayload> mailSendWriter(MailSendWriterClassifier classifier) {
        ClassifierCompositeItemWriter<AbstractMailPayload> writer = new ClassifierCompositeItemWriter<>();
        writer.setClassifier(classifier);

        return writer;
    }

    @Bean
    public Step managerMailSendStep(
            Step mailSendStep,
            MailSendPartitioner partitioner,
            TaskExecutor partitionTaskExecutor
    ) {
        return new StepBuilder("managerMailSendStep", jobRepository)
                .partitioner("mailSendStep", partitioner)
                .step(mailSendStep)
                .taskExecutor(partitionTaskExecutor)
                .gridSize(POOL_SIZE)
                .build();
    }

    @Bean
    public Step mailSendStep(
            JdbcPagingItemReader<ForwardLog> mailSendReader,
            ForwardProcessor forwardProcessor,
            ForwardWriter forwardWriter
    ) {
        return new StepBuilder("mailSendStep", jobRepository)
                .<ForwardLog, ForwardLog>chunk(CHUNK_SIZE, transactionManager)
                .reader(mailSendReader)
                .processor(forwardProcessor)
                .writer(forwardWriter)
                .build();
    }

    @Bean
    public TaskExecutor partitionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(POOL_SIZE);
        executor.setMaxPoolSize(POOL_SIZE);
        executor.setThreadNamePrefix("partition-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
        executor.initialize();

        return executor;
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<ForwardLog> mailSendReader(
            @Value("#{jobParameters['datetime']}") LocalDateTime dateTime,
            @Value("#{stepExecutionContext['startId'] ?: 1L}") Long startId,
            @Value("#{stepExecutionContext['endId'] ?: T(java.lang.Long).MAX_VALUE}") Long endId,
            ForwardReader forwardReader
    ) {
        return forwardReader.generate(dateTime, dateTime.plusDays(1), startId, endId);
    }

    @Bean
    public Step changeSequenceStep(ChangeSequenceTasklet changeSequenceTasklet) {
        return new StepBuilder("changeSequenceTasklet", jobRepository)
                .tasklet(changeSequenceTasklet, transactionManager)
                .build();
    }
}
