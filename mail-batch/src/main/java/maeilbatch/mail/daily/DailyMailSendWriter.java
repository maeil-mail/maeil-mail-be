package maeilbatch.mail.daily;

import lombok.RequiredArgsConstructor;
import maeilmail.bulksend.sender.QuestionSender;
import maeilmail.bulksend.sender.SubscribeQuestionMessage;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyMailSendWriter implements ItemWriter<SubscribeQuestionMessage> {

    private final QuestionSender questionSender;

    @Override
    public void write(Chunk<? extends SubscribeQuestionMessage> chunk) {
        for (SubscribeQuestionMessage message : chunk) {
            questionSender.sendMail(message);
        }
    }
}
