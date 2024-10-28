package maeilmail.subscribe.core;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.DistributedSupport;
import maeilmail.mail.MailMessage;
import maeilmail.mail.MailSender;
import maeilmail.question.QuestionSummary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class SendQuestionScheduler {

    private final MailSender mailSender;
    private final ChoiceQuestionPolicy choiceQuestionPolicy;
    private final SubscribeQuestionView subscribeQuestionView;
    private final SubscribeRepository subscribeRepository;
    private final DistributedSupport distributedSupport;

    @Scheduled(cron = "0 0 7 1/1 * ?", zone = "Asia/Seoul")
    public void sendMail() {
        log.info("메일 전송을 시작합니다.");
        List<Subscribe> subscribes = subscribeRepository.findAll();
        log.info("{}명의 사용자에게 메일을 전송합니다.", subscribes.size());

        subscribes.stream()
                .filter(it -> distributedSupport.isMine(it.getId()))
                .map(this::choiceQuestion)
                .filter(Objects::nonNull)
                .forEach(mailSender::sendMail);
    }

    private MailMessage choiceQuestion(Subscribe subscribe) {
        String subject = "오늘의 면접 질문을 보내드려요.";
        try {
            QuestionSummary question = choiceQuestionPolicy.choice(subscribe, LocalDate.now());
            String text = createText(question);
            return new MailMessage(subscribe.getEmail(), subject, text, subscribeQuestionView.getType());

        } catch (Exception e) {
            log.info("면접 질문 선택 실패 = {}", e.getMessage());
            return null;
        }
    }

    private String createText(QuestionSummary question) {
        HashMap<Object, Object> attribute = new HashMap<>();
        attribute.put("questionId", question.id());
        attribute.put("question", question.title());

        return subscribeQuestionView.render(attribute);
    }
}
