package maeilbatch.mail.weekly;

import lombok.RequiredArgsConstructor;
import maeilmail.bulksend.sender.WeeklyQuestionSender;
import maeilmail.bulksend.sender.WeeklySubscribeQuestionMessage;
import maeilmail.mail.MailMessage;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyMailSendWriter implements ItemWriter<MailMessage> {

    private final WeeklyQuestionSender weeklyQuestionSender;

    @Override
    public void write(Chunk<? extends MailMessage> chunk) {
        for (Object message : chunk) {
            weeklyQuestionSender.sendMailSync((WeeklySubscribeQuestionMessage) message);
        }
    }
}
