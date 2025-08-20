package maeilbatch.mail.policy;

import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilbatch.mail.ChoiceQuestionPolicy;
import maeilmail.question.QuestionQueryService;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.command.domain.Subscribe;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@RequiredArgsConstructor
class PersonalSequenceChoicePolicy implements ChoiceQuestionPolicy {

    private final QuestionQueryService questionQueryService;

    @Override
    public QuestionSummary choice(Subscribe subscribe) {
        List<QuestionSummary> questions = findQuestions(subscribe);
        Long nextQuestionSequence = subscribe.getNextQuestionSequence();

        return choiceQuestionBySequence(questions, nextQuestionSequence);
    }

    @Override
    public QuestionSummary choiceByRound(Subscribe subscribe, int round) {
        List<QuestionSummary> questions = findQuestions(subscribe);
        Long nextQuestionSequence = subscribe.getNextQuestionSequence() + round;

        return choiceQuestionBySequence(questions, nextQuestionSequence);
    }

    private List<QuestionSummary> findQuestions(Subscribe subscribe) {
        List<QuestionSummary> questions = questionQueryService.queryAllByCategory(subscribe.getCategory().name());
        validateQuestionEmpty(questions);

        return questions;
    }

    private void validateQuestionEmpty(List<QuestionSummary> questions) {
        if (questions.isEmpty()) {
            throw new IllegalStateException("질문지를 결정할 수 없습니다.");
        }
    }

    private QuestionSummary choiceQuestionBySequence(List<QuestionSummary> questions, Long nextQuestionSequence) {
        return questions.get(nextQuestionSequence.intValue() % questions.size());
    }
}
