package maeilmail.statistics;

public record MailEventReport(String type, Long success, Long fail) {
}
