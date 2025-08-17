package maeilmail.mail;

public record SimpleMailMessage(
        String to,
        String subject,
        String text,
        String type
) implements MailMessage {

    @Override
    public String getTo() {
        return to;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getSubject() {
        return subject;
    }
}
