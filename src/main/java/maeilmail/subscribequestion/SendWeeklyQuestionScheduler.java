package maeilmail.subscribequestion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.DistributedSupport;
import maeilmail.question.Question;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionQueryService;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.Subscribe;
import maeilmail.subscribe.SubscribeFrequency;
import maeilmail.subscribe.SubscribeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class SendWeeklyQuestionScheduler {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final int WEEKLY_MAIL_SEND_COUNT = 5;

    private final ChoiceQuestionPolicy choiceQuestionPolicy;
    private final WeeklyQuestionSender weeklyQuestionSender;
    private final WeeklySubscribeQuestionView weeklySubscribeQuestionView;
    private final SubscribeRepository subscribeRepository;
    private final DistributedSupport distributedSupport;
    private final QuestionQueryService questionQueryService;

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

        return subscribeRepository.findAllByCreatedAtBeforeAndDeletedAtIsNullAndFrequency(now, SubscribeFrequency.WEEKLY);
    }

    // TODO : subscribe의 frequency 필드를 이용해서 choiceQuestionPolicy 자체를 개선
    private WeeklySubscribeQuestionMessage choiceQuestion(Subscribe subscribe) {
        try {
            QuestionSummary baseQuestionSummary = choiceQuestionPolicy.choice(subscribe, LocalDate.now());
            List<QuestionSummary> questions = choiceWeeklyQuestions(subscribe, baseQuestionSummary);
            String subject = "금주의 면접 질문을 보내드려요.";
            String text = createText(subscribe, questions);

            return createWeeklySubscribeQuestionMessage(subscribe, questions, subject, text);
        } catch (Exception e) {
            log.info("면접 질문 선택 실패 = {}", e.getMessage());
            return null;
        }
    }

    private List<QuestionSummary> choiceWeeklyQuestions(Subscribe subscribe, QuestionSummary baseQuestionSummary) {
        List<QuestionSummary> questions = questionQueryService.queryAllByCategory(subscribe.getCategory().name());
        int fromIndex = questions.indexOf(baseQuestionSummary);

        return questions.subList(fromIndex, fromIndex + WEEKLY_MAIL_SEND_COUNT);
    }

    private String createText(Subscribe subscribe, List<QuestionSummary> questions) {
        LocalDate today = LocalDate.now();
        HashMap<Object, Object> attribute = new HashMap<>();
        attribute.put("questions", questions);
        attribute.put("email", subscribe.getEmail());
        attribute.put("token", subscribe.getToken());
        attribute.put("weekLabel", createWeekLabel(subscribe, today));
        attribute.put("year", today.getYear());
        attribute.put("month", today.getMonthValue());
        attribute.put("week", getWeekOfMonth(today));

        return weeklySubscribeQuestionView.render(attribute);
    }

    private WeeklySubscribeQuestionMessage createWeeklySubscribeQuestionMessage(
            Subscribe subscribe,
            List<QuestionSummary> questions,
            String subject,
            String text
    ) {
        List<Question> list = questions.stream()
                .map(QuestionSummary::toQuestion)
                .toList();

        return new WeeklySubscribeQuestionMessage(subscribe, list, subject, text);
    }

    private String createWeekLabel(Subscribe subscribe, LocalDate date) {
        int weekOfMonth = getWeekOfMonth(date);
        String category = subscribe.getCategory() == QuestionCategory.BACKEND ? "BE" : "FE";

        return category + " " + date.getMonthValue() + "월 " + weekOfMonth + "주차 질문";
    }

    private int getWeekOfMonth(LocalDate today) {
        WeekFields weekFields = WeekFields.of(Locale.KOREA);

        return today.get(weekFields.weekOfMonth());
    }
}
