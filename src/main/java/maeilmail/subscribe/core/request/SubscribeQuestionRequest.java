package maeilmail.subscribe.core.request;

import java.util.List;

public record SubscribeQuestionRequest(String email, List<String> category, String code) {
}
