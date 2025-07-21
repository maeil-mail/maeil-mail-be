package maeilwiki.mutiplechoice.application;

import maeilwiki.mutiplechoice.domain.Option;
import maeilwiki.mutiplechoice.domain.WorkbookQuestion;

public record OptionRequest(String content, boolean isCorrectAnswer) {

    public Option toOption(WorkbookQuestion question) {
        return new Option(content, isCorrectAnswer, question);
    }
}
