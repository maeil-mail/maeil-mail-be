package maeilmail.subscribe;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.mail.MailMessage;
import maeilmail.mail.MailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
class SubscribeVerifyService {

    private final VerifyCodeGenerator codeGenerator;
    private final VerifyMailView verifyMailView;
    private final MailSender mailSender;
    private final TemporalSubscribeManager temporalSubscribeManager;

    public void sendCodeIncludedMail(VerifyEmailRequest request) {
        String subject = "이메일 인증을 진행해주세요.";
        String code = codeGenerator.generateCode();
        String text = createText(code);
        MailMessage mailMessage = new MailMessage(request.email(), subject, text, verifyMailView.getType());

        log.info("인증 코드 포함 메일 요청, 이메일 = {} 코드 = {}", request.email(), code);
        mailSender.sendMail(mailMessage);

        temporalSubscribeManager.add(request.email(), code);
    }

    private String createText(String code) {
        Map<Object, Object> attribute = new HashMap<>();
        attribute.put("code", code);

        return verifyMailView.render(attribute);
    }

    public void verify(String email, String code) {
        temporalSubscribeManager.verify(email, code);
    }
}
