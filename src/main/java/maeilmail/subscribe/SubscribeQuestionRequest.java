package maeilmail.subscribe;

import java.util.List;

record SubscribeQuestionRequest(String email, List<String> category, String code) {
}
