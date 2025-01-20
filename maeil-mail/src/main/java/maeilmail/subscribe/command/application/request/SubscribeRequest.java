package maeilmail.subscribe.command.application.request;

import java.util.List;
import maeilmail.subscribe.command.domain.SubscribeFrequency;

public record SubscribeRequest(
        String email,
        List<String> category,
        String code,
        String frequency
) {

    // frequency 필드가 null로 들어올 하위 버전과 호환을 맞추기 위함
    public SubscribeRequest {
        if (frequency == null) {
            frequency = SubscribeFrequency.DAILY.toLowerCase();
        }
    }
}
