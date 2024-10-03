package maeilmail.subscribe;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class TemporalSubscriberStore {

    private static final String INVALID_EMAIL_MESSAGE = "인증되지 않은 이메일입니다.";
    private static final Map<String, String> store = new ConcurrentHashMap<>();

    public static void add(String email, String code) {
        store.put(email, code);
    }

    public static void verify(String email, String code) {
        if (!store.containsKey(email)) {
            throw new IllegalStateException(INVALID_EMAIL_MESSAGE);
        }

        String verifyCode = store.get(email);
        if (!verifyCode.equals(code)) {
            throw new IllegalStateException(INVALID_EMAIL_MESSAGE);
        }
    }
}
