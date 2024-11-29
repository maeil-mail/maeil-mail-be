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
        List<String> categories = request.category();

        for (String category : categories) {
            subscribeIfAbsent(request.email(), QuestionCategory.from(category), SubscribeFrequency.from(request.frequency()));
        }
    }

    private void subscribeIfAbsent(String email, QuestionCategory category, SubscribeFrequency frequency) {
        List<Subscribe> subscribes = subscribeRepository.findAllByEmailAndDeletedAtIsNull(email);
        boolean alreadyExist = subscribes.stream()
                .anyMatch(it -> it.getCategory() == category);

        if (!alreadyExist) {
            Subscribe subscribe = new Subscribe(email, category, frequency);
            subscribeRepository.save(subscribe);
            subscribes.forEach(it -> it.changeFrequency(frequency)); // 전송 주기 통일 작업
        }
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
