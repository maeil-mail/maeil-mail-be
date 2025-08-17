package maeilmail.subscribe.command.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.mail.MailMessage;
import maeilmail.mail.MailSender;
import maeilmail.mail.SimpleMailMessage;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.command.application.request.SubscribeRequest;
import maeilmail.subscribe.command.application.request.VerifyEmailRequest;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import maeilmail.subscribe.view.SubscribeWelcomeView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscribeService {

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
        List<QuestionCategory> newSubscribeCategories = findNewSubscribeCategories(request, subscribes);
        if (newSubscribeCategories.isEmpty()) {
            return;
        }

        newSubscribeCategories.forEach(it -> subscribe(it, request));
        synchronizeFrequency(request, subscribes);
    }

    private List<QuestionCategory> findNewSubscribeCategories(SubscribeRequest request, List<Subscribe> subscribes) {
        return request.category().stream()
                .map(QuestionCategory::from)
                .filter(it -> isNotSubscribed(it, subscribes))
                .toList();
    }

    private boolean isNotSubscribed(QuestionCategory category, List<Subscribe> subscribes) {
        return subscribes.stream()
                .noneMatch(it -> it.getCategory() == category);
    }

    private void subscribe(QuestionCategory category, SubscribeRequest request) {
        SubscribeFrequency frequency = SubscribeFrequency.from(request.frequency());
        Subscribe subscribe = new Subscribe(request.email(), category, frequency);
        subscribeRepository.save(subscribe);
    }

    private void synchronizeFrequency(SubscribeRequest request, List<Subscribe> subscribes) {
        SubscribeFrequency frequency = SubscribeFrequency.from(request.frequency());
        subscribes.forEach(it -> it.changeFrequency(frequency));
    }

    public void sendCodeIncludedMail(VerifyEmailRequest request) {
        verifySubscribeService.sendCodeIncludedMail(request);
    }

    private void sendSubscribeWelcomeMail(String email) {
        String subject = "앞으로 면접 질문을 보내드릴게요.";
        String text = createText();
        SimpleMailMessage mailMessage = new SimpleMailMessage(email, subject, text, welcomeView.getType());
        mailSender.sendMail(mailMessage);
    }

    private String createText() {
        Map<Object, Object> attribute = new HashMap<>();

        return welcomeView.render(attribute);
    }
}
