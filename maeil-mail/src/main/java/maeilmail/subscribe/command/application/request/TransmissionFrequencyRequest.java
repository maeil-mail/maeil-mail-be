package maeilmail.subscribe.command.application.request;

public record TransmissionFrequencyRequest(String email, String token, String frequency) {
}
