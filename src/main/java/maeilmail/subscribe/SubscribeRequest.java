package maeilmail.subscribe;

import java.util.List;

// TODO : 하위호환성 고려해서 frequency 필드 받도록 구현
record SubscribeRequest(
        String email,
        List<String> category,
        String code,
        String frequency) {

}
