package maeilmail.subscribe.command.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.mail.MailSender;
import maeilmail.mail.MailView;
import maeilmail.mail.MailViewRenderer;
import maeilmail.mail.SimpleMailMessage;
import maeilmail.subscribe.command.application.request.VerifyEmailRequest;
import maeilmail.subscribe.view.VerifyMailView;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifySubscribeService {

    private final VerifyCodeGenerator codeGenerator;
    private final MailSender mailSender;
    private final TemporalSubscribeManager temporalSubscribeManager;
    private final MailViewRenderer mailViewRenderer;

    public void sendCodeIncludedMail(VerifyEmailRequest request) {
        String subject = "이메일 인증을 진행해주세요.";
        String code = codeGenerator.generateCode();
        MailView view = createView(code);
        String text = view.render();

        SimpleMailMessage mailMessage = new SimpleMailMessage(request.email(), subject, text, view.getType());

        log.info("인증 코드 포함 메일 요청, 이메일 = {} 코드 = {}", request.email(), code);
        mailSender.sendMail(mailMessage);

        temporalSubscribeManager.add(request.email(), code);
    }

    private MailView createView(String code) {
        return VerifyMailView.builder()
                .renderer(mailViewRenderer)
                .verifyCode(code)
                .build();
    }

    public void verify(String email, String code) {
        temporalSubscribeManager.verify(email, code);
    }
}
