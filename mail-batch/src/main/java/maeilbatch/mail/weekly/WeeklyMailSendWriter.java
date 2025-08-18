package maeilbatch.mail.weekly;

import lombok.RequiredArgsConstructor;
import maeilmail.mail.MailMessage;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyMailSendWriter implements ItemWriter<MailMessage> {

    private final WeeklyMailSender weeklyMailSender;

    @Override
    public void write(Chunk<? extends MailMessage> chunk) {
        for (MailMessage message : chunk) {
            weeklyMailSender.sendMailSync((WeeklyMailMessage) message);
        }
    }
}
