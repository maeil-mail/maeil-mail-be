package maeilbatch.mail.daily;

import lombok.RequiredArgsConstructor;
import maeilmail.bulksend.sender.QuestionSender;
import maeilmail.bulksend.sender.SubscribeQuestionMessage;
import maeilmail.mail.MailMessage;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyMailSendWriter implements ItemWriter<MailMessage> {

    private final QuestionSender questionSender;

    @Override
    public void write(Chunk<? extends MailMessage> chunk) {
        for (Object message : chunk) {
            questionSender.sendMailSync((SubscribeQuestionMessage) message);
        }
    }
}
