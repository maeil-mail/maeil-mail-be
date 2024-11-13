package maeilmail.subscribe;

import java.util.List;

record SubscribeRequest(String email, List<String> category, String code) {
}
