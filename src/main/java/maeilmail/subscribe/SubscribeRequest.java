package maeilmail.subscribe;

import java.util.List;

record SubscribeRequest(
        String email,
        List<String> category,
        String code,
        String frequency
) {

    // frequency 필드가 null로 들어올 하위 버전과 호환을 맞추기 위함
    SubscribeRequest {
        if (frequency == null) {
            frequency = SubscribeFrequency.DAILY.toLowerCase();
        }
    }
}
