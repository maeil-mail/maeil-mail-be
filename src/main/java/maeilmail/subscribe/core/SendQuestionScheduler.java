package maeilmail.subscribe.core;

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
import maeilmail.mail.MailMessage;
import maeilmail.mail.MailSender;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionSummary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class SendQuestionScheduler {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final MailSender mailSender;
    private final ChoiceQuestionPolicy choiceQuestionPolicy;
    private final SubscribeQuestionView subscribeQuestionView;
    private final SubscribeRepository subscribeRepository;
    private final DistributedSupport distributedSupport;

    @Scheduled(cron = "0 0 7 * * MON-FRI", zone = "Asia/Seoul")
    public void sendMail() {
        log.info("메일 전송을 시작합니다.");
        LocalDateTime now = ZonedDateTime.now(KOREA_ZONE).toLocalDateTime();
        List<Subscribe> subscribes = subscribeRepository.findAllByCreatedAtBefore(now);
        log.info("{}명의 사용자에게 메일을 전송합니다.", subscribes.size());

        subscribes.stream()
                .filter(it -> distributedSupport.isMine(it.getId()))
                .map(this::choiceQuestion)
                .filter(Objects::nonNull)
                .forEach(mailSender::sendMail);
    }

    private MailMessage choiceQuestion(Subscribe subscribe) {
        try {
            QuestionSummary question = choiceQuestionPolicy.choice(subscribe, LocalDate.now());
            String subject = createSubject(question);
            String text = createText(question);
            return new MailMessage(subscribe.getEmail(), subject, text, createQuestionType(subscribe.getCategory()));
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

    private String createText(QuestionSummary question) {
        HashMap<Object, Object> attribute = new HashMap<>();
        attribute.put("questionId", question.id());
        attribute.put("question", question.title());

        return subscribeQuestionView.render(attribute);
    }

    private String createQuestionType(QuestionCategory category) {
        String type = subscribeQuestionView.getType();
        if (QuestionCategory.BACKEND.equals(category)) {
            return type + "-backend";
        }

        return type + "-frontend";
    }
}
