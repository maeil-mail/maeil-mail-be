package maeilwiki.mutiplechoice.application;

public record MultipleChoiceOptionRequest(
        String content,
        boolean isCorrectAnswer
) {
}
