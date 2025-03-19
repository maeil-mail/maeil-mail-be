package maeilwiki.wiki.application;

import maeilwiki.wiki.domain.MultipleChoiceQuestion;
import maeilwiki.wiki.domain.MultipleChoiceWiki;
import java.util.List;

public record MultipleChoiceWikiRequest(
        String title,
        String content,
        String category,
        boolean isAnonymous,
        int difficultyLevel,
        List<MultipleChoiceQuestionRequest> multipleChoiceQuestions,
        Long memberId
) {

    public MultipleChoiceWiki toMultipleChoiceWiki(Long memberId) {
        List<MultipleChoiceQuestion> questions = multipleChoiceQuestions.stream().map(MultipleChoiceQuestionRequest::toMultipleQuestion).toList();

        if (content == null || content.isBlank()) {
            return new MultipleChoiceWiki(title, "", category, isAnonymous, difficultyLevel, questions, memberId);
        }

        return new MultipleChoiceWiki(title, content, category, isAnonymous, difficultyLevel, questions, memberId);
    }

    record MultipleChoiceQuestionRequest(String content, boolean isAnswer) {
        MultipleChoiceQuestion toMultipleQuestion() {
            return new MultipleChoiceQuestion(content, isAnswer);
        }
    }
}
