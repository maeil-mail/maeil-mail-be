package maeilmail.subscribe.command.application;

import maeilmail.mail.MailMessage;
import maeilmail.mail.MailSender;
import maeilmail.subscribe.command.domain.SubscribeCreatedEvent;
import maeilmail.subscribe.command.domain.SubscribeEventListener;
import maeilmail.subscribe.view.SubscribeWelcomeView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

public class SubscribeEventListenerTest {

    private final MailSender mailSender = mock(MailSender.class);
    private final SubscribeWelcomeView welcomeView = mock(SubscribeWelcomeView.class);
    private final SubscribeEventListener listener = new SubscribeEventListener(mailSender, welcomeView);

    @Test
    @DisplayName("SubscribeCreatedEvent를 받으면 웰컴 메일을 발송한다.")
    void handleEvent() {
        SubscribeCreatedEvent event = new SubscribeCreatedEvent("test@naver.com");
        when(welcomeView.render(anyMap())).thenReturn("welcome text");
        when(welcomeView.getType()).thenReturn("welcom");

        listener.sendWelcomeMail(event);

        verify(mailSender).sendMail(any(MailMessage.class));
    }
}
