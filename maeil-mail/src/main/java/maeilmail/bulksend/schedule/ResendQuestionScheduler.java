package maeilmail.bulksend.schedule;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.bulksend.sender.QuestionSender;
import maeilmail.bulksend.sender.SubscribeQuestionMessage;
import maeilmail.bulksend.view.SubscribeQuestionView;
import maeilmail.question.Question;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import maeilmail.subscribe.command.domain.SubscribeQuestionRepository;
import maeilmail.support.DistributedSupport;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// TODO: 간헐적으로 메일 전송이 실패하는 현상을 해결하기 이전까지 임시로 사용합니다. (데일리 전송 전용)
@Slf4j
@Component
@RequiredArgsConstructor
class ResendQuestionScheduler {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final QuestionSender questionSender;
    private final SubscribeQuestionView subscribeQuestionView;
    private final DistributedSupport distributedSupport;
    private final SubscribeQuestionRepository subscribeQuestionRepository;

    @Scheduled(cron = "0 25 7 * * MON-FRI", zone = "Asia/Seoul")
    public void sendMail() {
        log.info("임시 재전송 로직을 수행합니다. (일간)");
        List<SubscribeQuestion> subscribeQuestions = getFailedSubscribeQuestions();

        List<SubscribeQuestion> filteredSubscribeQuestions = subscribeQuestions.stream()
                .filter(this::isDaily)
                .filter(it -> distributedSupport.isMine(it.getId()))
                .toList();
        log.info("{}명의 일간 구독자에게 질문지를 재전송합니다.", filteredSubscribeQuestions.size());

        List<Long> removeTargetIds = filteredSubscribeQuestions.stream()
                .map(SubscribeQuestion::getId)
                .toList();
        subscribeQuestionRepository.removeAllByIdIn(removeTargetIds);

        filteredSubscribeQuestions.stream()
                .map(this::generateQuestionMessage)
                .forEach(questionSender::sendMail);
    }

    private boolean isDaily(SubscribeQuestion subscribeQuestion) {
        return subscribeQuestion.getSubscribe().getFrequency() == SubscribeFrequency.DAILY;
    }

    private List<SubscribeQuestion> getFailedSubscribeQuestions() {
        LocalDateTime baseDate = ZonedDateTime.now(KOREA_ZONE).toLocalDate().atStartOfDay();

        return subscribeQuestionRepository.findAllFailedSubscribeQuestions(baseDate);
    }

    private SubscribeQuestionMessage generateQuestionMessage(SubscribeQuestion subscribeQuestion) {
        Subscribe subscribe = subscribeQuestion.getSubscribe();
        Question question = subscribeQuestion.getQuestion();
        String subject = question.getTitle();
        String text = createText(subscribe, question);

        return new SubscribeQuestionMessage(subscribe, question, subject, text);
    }

    private String createText(Subscribe subscribe, Question question) {
        HashMap<Object, Object> attribute = new HashMap<>();
        attribute.put("questionId", question.getId());
        attribute.put("question", question.getTitle());
        attribute.put("email", subscribe.getEmail());
        attribute.put("token", subscribe.getToken());

        return subscribeQuestionView.render(attribute);
    }
}
