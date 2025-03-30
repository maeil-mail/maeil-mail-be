package maeilwiki.mutiplechoice.application;

import maeilwiki.mutiplechoice.domain.MultipleChoiceOption;
import maeilwiki.mutiplechoice.domain.MultipleChoiceQuestion;

public record MultipleChoiceOptionRequest(String content, boolean isCorrectAnswer) {

    public MultipleChoiceOption toOption(MultipleChoiceQuestion question) {
        return new MultipleChoiceOption(content, isCorrectAnswer, question);
    }
}
