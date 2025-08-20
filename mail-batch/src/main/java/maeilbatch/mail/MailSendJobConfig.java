package maeilbatch.mail;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.mail.MailMessage;
import maeilmail.subscribe.command.domain.Subscribe;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.support.ClassifierCompositeItemProcessor;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
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
    public Job mailSendJob(
            Step mailSendStep,
            Step changeSequenceStep,
            JobExecutionListener mailSendJobReportListener
    ) {
        return new JobBuilder("mailSendJob", jobRepository)
                .start(mailSendStep)
                .next(changeSequenceStep)
                .listener(mailSendJobReportListener)
                .build();
    }

    @Bean
    public Step mailSendStep(
            JpaCursorItemReader<Subscribe> subscribeReader,
            CompositeItemProcessor<Subscribe, MailMessage> mailSendProcessor,
            ClassifierCompositeItemWriter<MailMessage> mailSendWriter
    ) {
        return new StepBuilder("mailSendStep", jobRepository)
                .<Subscribe, MailMessage>chunk(CHUNK_SIZE, transactionManager)
                .reader(subscribeReader)
                .processor(mailSendProcessor)
                .writer(mailSendWriter)
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<Subscribe> subscribeReader(
            @Value("#{jobParameters['datetime']}") LocalDateTime dateTime,
            MailSendItemReader mailSendItemReader
    ) {
        return mailSendItemReader.generate(dateTime);
    }

    @Bean
    public CompositeItemProcessor<Subscribe, MailMessage> mailSendProcessor(
            FilterSubscribeProcessor filterSubscribeProcessor,
            ClassifierCompositeItemProcessor<Subscribe, MailMessage> mailMessageProcessor
    ) {
        CompositeItemProcessor<Subscribe, MailMessage> mailSendProcessor = new CompositeItemProcessor<>();
        mailSendProcessor.setDelegates(List.of(filterSubscribeProcessor, mailMessageProcessor));

        return mailSendProcessor;
    }

    @Bean
    public ClassifierCompositeItemProcessor<Subscribe, MailMessage> mailMessageProcessor(
            MailSendProcessorClassifier classifier
    ) {
        ClassifierCompositeItemProcessor<Subscribe, MailMessage> processor = new ClassifierCompositeItemProcessor<>();
        processor.setClassifier(classifier);

        return processor;
    }

    @Bean
    public ClassifierCompositeItemWriter<MailMessage> mailSendWriter(MailSendWriterClassifier classifier) {
        ClassifierCompositeItemWriter<MailMessage> writer = new ClassifierCompositeItemWriter<>();
        writer.setClassifier(classifier);

        return writer;
    }

    @Bean
    public Step changeSequenceStep(ChangeSequenceTasklet changeSequenceTasklet) {
        return new StepBuilder("changeSequenceTasklet", jobRepository)
                .tasklet(changeSequenceTasklet, transactionManager)
                .build();
    }
}
