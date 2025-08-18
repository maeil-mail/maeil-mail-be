package maeilmail.bulksend.schedule;

import static maeilmail.subscribe.command.domain.SubscribeFrequency.WEEKLY;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.bulksend.sender.ChoiceQuestionPolicy;
import maeilmail.bulksend.sender.WeeklyQuestionSender;
import maeilmail.bulksend.sender.WeeklySubscribeQuestionMessage;
import maeilmail.bulksend.view.WeeklySubscribeQuestionView;
import maeilmail.mail.MailView;
import maeilmail.mail.MailViewRenderer;
import maeilmail.question.Question;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import maeilmail.utils.DistributedSupport;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendWeeklyQuestionScheduler {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final ChoiceQuestionPolicy choiceQuestionPolicy;
    private final WeeklyQuestionSender weeklyQuestionSender;
    private final SubscribeRepository subscribeRepository;
    private final DistributedSupport distributedSupport;
    private final MailViewRenderer mailViewRenderer;

    @Scheduled(cron = "0 0 7 * * MON", zone = "Asia/Seoul")
    public void sendMail() {
        log.info("주간 메일 구독자에게 질문지 발송을 시작합니다.");
        List<Subscribe> subscribes = getSubscribes();
        log.info("{}명의 주간 메일 구독자에게 질문지를 발송합니다.", subscribes.size());

        subscribes.stream()
                .filter(it -> distributedSupport.isMine(it.getId()))
                .map(this::choiceQuestion)
                .filter(Objects::nonNull)
                .forEach(weeklyQuestionSender::sendMail);
    }

    private List<Subscribe> getSubscribes() {
        LocalDateTime now = ZonedDateTime.now(KOREA_ZONE).toLocalDateTime();

        return subscribeRepository.findAllByCreatedAtBeforeAndDeletedAtIsNullAndFrequency(now, WEEKLY);
    }

    private WeeklySubscribeQuestionMessage choiceQuestion(Subscribe subscribe) {
        try {
            List<QuestionSummary> questions = choiceWeeklyQuestions(subscribe);
            String subject = createSubject();
            String text = createText(subscribe, questions);
            return createWeeklySubscribeQuestionMessage(subscribe, questions, subject, text);
        } catch (Exception e) {
            log.error("주간 면접 질문 선택 실패. 구독자 id = {}", subscribe.getId(), e);
            return null;
        }
    }

    private List<QuestionSummary> choiceWeeklyQuestions(Subscribe subscribe) {
        return IntStream.range(0, WEEKLY.getSendCount())
                .mapToObj(round -> choiceQuestionPolicy.choiceByRound(subscribe, round))
                .toList();
    }

    private String createSubject() {
        return "이번주 면접 질문을 보내드려요.";
    }

    public String createText(Subscribe subscribe, List<QuestionSummary> questions) {
        LocalDate date = LocalDate.now();
        MailView view = WeeklySubscribeQuestionView.builder()
                .renderer(mailViewRenderer)
                .date(date)
                .questionSummaries(questions)
                .subscribe(subscribe)
                .build();


        return view.render();
    }

    public WeeklySubscribeQuestionMessage createWeeklySubscribeQuestionMessage(
            Subscribe subscribe,
            List<QuestionSummary> summaries,
            String subject,
            String text
    ) {
        List<Question> questions = summaries.stream()
                .map(QuestionSummary::toQuestion)
                .toList();

        return new WeeklySubscribeQuestionMessage(subscribe, questions, subject, text);
    }
}
