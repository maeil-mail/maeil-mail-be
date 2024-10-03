package maeilmail.subscribe;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.question.QuestionCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
class SubscribeQuestionService {

    private final SubscribeRepository subscribeRepository;
    private final EmailSender emailSender;
    private final VerifyEmailView verifyEmailView;

    public void sendCodeIncludedMail(VerifyEmailRequest request) {
        String subject = "이메일 인증을 진행해주세요.";
        String code = CodeGenerator.generateCode();
        String text = createText(code);
        MailMessage mailMessage = new MailMessage(request.email(), subject, text);

        log.info("인증 코드 포함 메일 요청, 이메일 = {} 코드 = {}", request.email(), code);
        emailSender.sendMail(mailMessage);

        TemporalSubscriberStore.add(request.email(), code);
    }

    @Transactional
    public void subscribe(SubscribeQuestionRequest request) {
        log.info("이메일 구독 요청, 이메일 = {}", request.email());

        TemporalSubscriberStore.verify(request.email(), request.code());
        QuestionCategory category = QuestionCategory.from(request.category());
        Subscribe subscribe = new Subscribe(request.email(), category);

        log.info("이메일 구독 성공, 이메일 = {}", request.email());
        subscribeRepository.save(subscribe);
    }

    private String createText(String code) {
        Map<Object, Object> attribute = new HashMap<>();
        attribute.put("code", code);

        return verifyEmailView.render(attribute);
    }
}
