package maeilwiki.mutiplechoice.application;

public record OptionResponse(
        Long id,
        String content,
        boolean isCorrectAnswer
) {
}
