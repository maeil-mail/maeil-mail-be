package maeilmail.subscribe.command.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.mail.MailSender;
import maeilmail.question.CategoryPolicy;
import maeilmail.question.CategoryPolicyRepository;
import maeilmail.mail.MailView;
import maeilmail.mail.MailViewRenderer;
import maeilmail.mail.SimpleMailMessage;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionRepository;
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
    private final MailSender mailSender;
    private final MailViewRenderer mailViewRenderer;

    private final CategoryPolicyRepository categoryPolicyRepository;
    private final QuestionRepository questionRepository;

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

        CategoryPolicy policy = categoryPolicyRepository.findByCategory(category)
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리의 초기 정책이 설정되어 있지 않습니다."));

        Long startQuestionId = policy.getStartQuestion().getId();
        long nextSequence = questionRepository.countByCategoryAndIdLessThan(category, startQuestionId);

        Subscribe subscribe = new Subscribe(request.email(), category, frequency, nextSequence);
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
        MailView view = createView();
        String text = view.render();
        SimpleMailMessage mailMessage = new SimpleMailMessage(email, subject, text, view.getType());
        mailSender.sendMail(mailMessage);
    }

    private MailView createView() {
        return SubscribeWelcomeView.builder()
                .renderer(mailViewRenderer)
                .build();
    }
}
