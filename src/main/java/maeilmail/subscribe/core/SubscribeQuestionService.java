package maeilmail.subscribe.core;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.mail.MailMessage;
import maeilmail.mail.MailSender;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.core.request.SubscribeQuestionRequest;
import maeilmail.subscribe.core.request.VerifyEmailRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscribeQuestionService {

    private final SubscribeRepository subscribeRepository;
    private final SubscribeVerifyService subscribeVerifyService;
    private final SubscribeWelcomeView welcomeView;
    private final MailSender mailSender;

    @Transactional
    public void subscribe(SubscribeQuestionRequest request) {
        log.info("이메일 구독 요청, 이메일 = {}", request.email());

        subscribeVerifyService.verify(request.email(), request.code());
        QuestionCategory category = QuestionCategory.from(request.category());
        Subscribe subscribe = new Subscribe(request.email(), category);

        log.info("이메일 구독 성공, 이메일 = {}", request.email());
        subscribeRepository.save(subscribe);
        sendSubscribeWelcomeMail(request.email());
    }

    public void sendCodeIncludedMail(VerifyEmailRequest request) {
        subscribeVerifyService.sendCodeIncludedMail(request);
    }

    private void sendSubscribeWelcomeMail(String email) {
        String subject = "지금부터 매일 면접 질문을 보내드릴게요.";
        String text = createText();
        MailMessage mailMessage = new MailMessage(email, subject, text, welcomeView.getType());
        mailSender.sendMail(mailMessage);
    }

    private String createText() {
        Map<Object, Object> attribute = new HashMap<>();

        return welcomeView.render(attribute);
    }
}
