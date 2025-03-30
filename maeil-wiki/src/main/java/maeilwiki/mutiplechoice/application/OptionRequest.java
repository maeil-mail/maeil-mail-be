package maeilwiki.mutiplechoice.application;

import maeilwiki.mutiplechoice.domain.Option;
import maeilwiki.mutiplechoice.domain.Question;

public record OptionRequest(String content, boolean isCorrectAnswer) {

    public Option toOption(Question question) {
        return new Option(content, isCorrectAnswer, question);
    }
}
