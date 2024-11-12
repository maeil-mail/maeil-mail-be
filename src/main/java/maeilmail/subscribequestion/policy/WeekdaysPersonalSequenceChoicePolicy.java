package maeilmail.subscribequestion.policy;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilmail.question.QuestionQueryService;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.core.Subscribe;
import maeilmail.subscribequestion.ChoiceQuestionPolicy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@RequiredArgsConstructor
class WeekdaysPersonalSequenceChoicePolicy implements ChoiceQuestionPolicy {

    private final QuestionQueryService questionQueryService;

    @Override
    public QuestionSummary choice(Subscribe subscribe, LocalDate ignore) {
        List<QuestionSummary> questions = findQuestions(subscribe);
        Long nextQuestionSequence = subscribe.getNextQuestionSequence();

        return questions.get(nextQuestionSequence.intValue() % questions.size());
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
}
