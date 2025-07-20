package maeilmail.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import maeilmail.admin.domain.AdminNotice;
import maeilmail.admin.domain.AdminNoticeRepository;
import maeilmail.mail.MailMessage;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import maeilmail.support.IntegrationTestSupport;
import maeilmail.support.SchedulerTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;

class SendAdminNoticeSchedulerTest extends IntegrationTestSupport {

    @Autowired
    private SendAdminNoticeScheduler sendAdminNoticeScheduler;

    @Autowired
    private AdminNoticeRepository adminNoticeRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Test
    @DisplayName("매주 월요일에 관리자 공지 메일을 전송하는 스케줄러가 동작하는지 확인한다.")
    void sendMailEveryMonday() {
        // given
        LocalDateTime initialTime = LocalDateTime.of(2025, 2, 17, 8, 0);
        List<LocalDateTime> expectedTimes = List.of(
                LocalDateTime.of(2025, 2, 24, 8, 0),
                LocalDateTime.of(2025, 3, 3, 8, 0)
        );

        // when & then
        SchedulerTestUtils.assertCronExpression(
                SendAdminNoticeScheduler.class,
                "sendMail",
                toInstant(initialTime),
                expectedTimes.stream().map(this::toInstant).toList()
        );
    }

    private Instant toInstant(LocalDateTime time) {
        return time.atZone(ZoneId.of("Asia/Seoul")).toInstant();
    }

    @Test
    @DisplayName("스케줄러가 동작하는 날짜에 공지 예약이 없으면 메일을 발송하지 않는다.")
    void dontSendMail() {
        // given
        LocalDate reservedAt = LocalDate.of(2025, 1, 6);
        createAdminNotice(reservedAt);

        // when
        sendAdminNoticeScheduler.sendMail();

        // then
        verify(mailSender, never()).sendMail(any());
    }

    @Test
    @DisplayName("스케줄러가 동작하는 날짜에 공지 예약이 있으면 메일을 발송한다.")
    void sendMail() {
        // given
        createSubscribe();
        createAdminNotice(LocalDate.of(2025, 1, 13));
        createAdminNotice(LocalDate.of(2025, 1, 20));

        String title = "타겟 공지";
        LocalDate reservedAt = LocalDate.now();
        createAdminNotice(title, reservedAt);

        // when
        sendAdminNoticeScheduler.sendMail();

        // then
        ArgumentCaptor<MailMessage> captor = ArgumentCaptor.forClass(MailMessage.class);
        verify(mailSender).sendMail(captor.capture());
        assertThat(captor.getValue().subject()).isEqualTo(title);
    }

    @Test
    @DisplayName("중복된 이메일은 한 번만 메일을 발송한다.")
    void sendMailDistinctEmail() {
        // given
        String duplicatedEmail = "prin@email.com";
        createSubscribe(duplicatedEmail);
        createSubscribe(duplicatedEmail);
        createAdminNotice(LocalDate.now());

        // when
        sendAdminNoticeScheduler.sendMail();

        // then
        verify(mailSender, times(1)).sendMail(any());
    }

    private AdminNotice createAdminNotice(String title, LocalDate reservedAt) {
        AdminNotice adminNotice = new AdminNotice(null, title, "공지 내용", reservedAt);
        return adminNoticeRepository.save(adminNotice);
    }

    private AdminNotice createAdminNotice(LocalDate reservedAt) {
        return createAdminNotice("공지 제목", reservedAt);
    }

    private Subscribe createSubscribe(String email) {
        Subscribe subscribe = new Subscribe(email, QuestionCategory.BACKEND, SubscribeFrequency.DAILY);
        return subscribeRepository.save(subscribe);
    }

    private Subscribe createSubscribe() {
        return createSubscribe("test@email.com");
    }
}
