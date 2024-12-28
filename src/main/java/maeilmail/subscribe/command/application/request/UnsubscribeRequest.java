package maeilmail.subscribe.command.application.request;

public record UnsubscribeRequest(String email, String token) {
}
