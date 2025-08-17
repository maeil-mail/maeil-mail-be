package maeilbatch.jobconfig;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.bulksend.sender.ChoiceQuestionPolicy;
import maeilmail.bulksend.sender.QuestionSender;
import maeilmail.bulksend.sender.SubscribeQuestionMessage;
import maeilmail.bulksend.view.SubscribeQuestionView;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.command.domain.Subscribe;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
class DailyMailSendJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ChoiceQuestionPolicy choiceQuestionPolicy;
    private final SubscribeQuestionView subscribeQuestionView;
    private final QuestionSender questionSender;

    @Bean
    public Job dailyMailSendJob(Step dailyMailSendStep) {
        return new JobBuilder("dailyMailSendJob", jobRepository)
                .start(dailyMailSendStep)
                .next(changeSequenceStep())
                .next(adminReportSendStep())
                .build();
    }

    @Bean
    public Step dailyMailSendStep(JpaCursorItemReader<Subscribe> dailySubscribeReader) {
        return new StepBuilder("dailyMailSendStep", jobRepository)
                .<Subscribe, SubscribeQuestionMessage>chunk(100, transactionManager)
                .reader(dailySubscribeReader)
                .processor(dailySubscribeProcessor())
                .writer(dailyMailSendWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<Subscribe> dailySubscribeReader(
            EntityManagerFactory entityManagerFactory,
            @Value("#{jobParameters['datetime']}") LocalDateTime startedAt
    ) {
        return new JpaCursorItemReaderBuilder<Subscribe>()
                .name("dailySubscribeReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select s from Subscribe s where s.createdAt <= :createdAt and s.deletedAt is null and s.frequency = DAILY order by s.id ASC")
                .parameterValues(Map.of("createdAt", startedAt))
                .hintValues(Map.of("org.hibernate.fetchSize", 100))
                .build();
    }

    public ItemProcessor<Subscribe, SubscribeQuestionMessage> dailySubscribeProcessor() {
        return this::choiceQuestion;
    }

    private SubscribeQuestionMessage choiceQuestion(Subscribe subscribe) {
        try {
            QuestionSummary questionSummary = choiceQuestionPolicy.choice(subscribe);
            String subject = createSubject(questionSummary);
            String text = createText(subscribe, questionSummary);
            return new SubscribeQuestionMessage(subscribe, questionSummary.toQuestion(), subject, text);
        } catch (Exception e) {
            log.error("일간 면접 질문 선택 실패. 구독자 id = {}", subscribe.getId(), e);
            return null;
        }
    }

    private String createSubject(QuestionSummary question) {
        return question.title();
    }

    private String createText(Subscribe subscribe, QuestionSummary question) {
        HashMap<Object, Object> attribute = new HashMap<>();
        attribute.put("questionId", question.id());
        attribute.put("question", question.title());
        attribute.put("email", subscribe.getEmail());
        attribute.put("token", subscribe.getToken());

        return subscribeQuestionView.render(attribute);
    }

    public ItemWriter<SubscribeQuestionMessage> dailyMailSendWriter() {
        return chunk -> chunk.getItems()
                .forEach(questionSender::sendMailWithTransaction);
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
