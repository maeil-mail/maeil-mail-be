package maeilbatch.mail.weekly;

import lombok.RequiredArgsConstructor;
import maeilmail.bulksend.sender.WeeklyQuestionSender;
import maeilmail.bulksend.sender.WeeklySubscribeQuestionMessage;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class WeeklyMailSendWriter implements ItemWriter<WeeklySubscribeQuestionMessage> {

    private final WeeklyQuestionSender weeklyQuestionSender;

    @Override
    public void write(Chunk<? extends WeeklySubscribeQuestionMessage> chunk) {
        for (WeeklySubscribeQuestionMessage message : chunk) {
            weeklyQuestionSender.sendMail(message);
        }
    }
}
