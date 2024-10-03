package maeilmail.subscribe;

public record MailMessage(String to, String subject, String text, String type) {
}
