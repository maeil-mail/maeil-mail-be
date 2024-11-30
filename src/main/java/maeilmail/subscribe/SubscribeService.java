package maeilmail.subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.mail.MailMessage;
import maeilmail.mail.MailSender;
import maeilmail.question.QuestionCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
class SubscribeService {

    private final SubscribeRepository subscribeRepository;
    private final VerifySubscribeService verifySubscribeService;
    private final SubscribeWelcomeView welcomeView;
    private final MailSender mailSender;

    @Transactional
    public void subscribe(SubscribeRequest request) {
        log.info("이메일 구독 요청, 이메일 = {}", request.email());
        trySubscribe(request);
        sendSubscribeWelcomeMail(request.email());
        log.info("이메일 구독 성공, 이메일 = {}", request.email());
    }

    private void trySubscribe(SubscribeRequest request) {
        verifySubscribeService.verify(request.email(), request.code());
        List<Subscribe> subscribes = subscribeRepository.findAllByEmailAndDeletedAtIsNull(request.email());
        List<String> categories = request.category();

        categories.stream()
                .map(QuestionCategory::from)
                .filter(it -> isNotSubscribed(it, subscribes))
                .forEach(it -> subscribe(it, subscribes, request));
    }

    private boolean isNotSubscribed(QuestionCategory category, List<Subscribe> subscribes) {
        return subscribes.stream()
                .noneMatch(it -> it.getCategory() == category);
    }

    private void subscribe(QuestionCategory category, List<Subscribe> subscribes, SubscribeRequest request) {
        SubscribeFrequency frequency = SubscribeFrequency.from(request.frequency());
        Subscribe subscribe = new Subscribe(request.email(), category, frequency);
        subscribeRepository.save(subscribe);
        subscribes.forEach(it -> it.changeFrequency(frequency)); // 전송 주기 통일 작업
    }

    public void sendCodeIncludedMail(VerifyEmailRequest request) {
        verifySubscribeService.sendCodeIncludedMail(request);
    }

    private void sendSubscribeWelcomeMail(String email) {
        String subject = "앞으로 매일 면접 질문을 보내드릴게요.";
        String text = createText();
        MailMessage mailMessage = new MailMessage(email, subject, text, welcomeView.getType());
        mailSender.sendMail(mailMessage);
    }

    private String createText() {
        Map<Object, Object> attribute = new HashMap<>();

        return welcomeView.render(attribute);
    }
}
