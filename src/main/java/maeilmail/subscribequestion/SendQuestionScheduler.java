package maeilmail.subscribequestion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.DistributedSupport;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionQueryService;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.Subscribe;
import maeilmail.subscribe.SubscribeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class SendQuestionScheduler {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final QuestionSender questionSender;
    private final ChoiceQuestionPolicy choiceQuestionPolicy;
    private final SubscribeQuestionView subscribeQuestionView;
    private final SubscribeRepository subscribeRepository;
    private final DistributedSupport distributedSupport;
    private final QuestionQueryService questionQueryService;

    @Scheduled(cron = "0 0 7 * * MON-FRI", zone = "Asia/Seoul")
    public void sendMail() {
        cacheWarmUp();

        log.info("구독자에게 질문지 발송을 시작합니다.");
        LocalDateTime now = ZonedDateTime.now(KOREA_ZONE).toLocalDateTime();
        List<Subscribe> subscribes = subscribeRepository.findAllByCreatedAtBeforeAndDeletedAtIsNull(now);
        log.info("{}명의 구독자에게 질문지를 발송합니다.", subscribes.size());

        subscribes.stream()
                .filter(it -> distributedSupport.isMine(it.getId()))
                .map(this::choiceQuestion)
                .filter(Objects::nonNull)
                .forEach(questionSender::sendMail);
    }

    private void cacheWarmUp() {
        questionQueryService.queryAllByCategory(QuestionCategory.BACKEND.name());
        questionQueryService.queryAllByCategory(QuestionCategory.FRONTEND.name());
    }

    private SubscribeQuestionMessage choiceQuestion(Subscribe subscribe) {
        try {
            QuestionSummary questionSummary = choiceQuestionPolicy.choice(subscribe, LocalDate.now());
            String subject = createSubject(questionSummary);
            String text = createText(subscribe, questionSummary);
            return new SubscribeQuestionMessage(subscribe, questionSummary.toQuestion(), subject, text);
        } catch (Exception e) {
            log.info("면접 질문 선택 실패 = {}", e.getMessage());
            return null;
        }
    }

    private String createSubject(QuestionSummary question) {
        if (question.customizedTitle() == null) {
            return "오늘의 면접 질문을 보내드려요.";
        }

        return question.customizedTitle();
    }

    private String createText(Subscribe subscribe, QuestionSummary question) {
        HashMap<Object, Object> attribute = new HashMap<>();
        attribute.put("questionId", question.id());
        attribute.put("question", question.title());
        attribute.put("email", subscribe.getEmail());
        attribute.put("token", subscribe.getToken());

        return subscribeQuestionView.render(attribute);
    }
}
