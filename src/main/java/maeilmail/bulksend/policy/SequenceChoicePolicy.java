package maeilmail.bulksend.policy;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilmail.bulksend.ChoiceQuestionPolicy;
import maeilmail.question.QuestionQueryService;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.command.domain.Subscribe;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SequenceChoicePolicy implements ChoiceQuestionPolicy {

    private final LocalDate baseDate = LocalDate.of(2024, 9, 23);
    private final QuestionQueryService questionQueryService;

    @Override
    public QuestionSummary choice(Subscribe subscribe, LocalDate today) {
        validateInvalidDate(today);
        List<QuestionSummary> questions = findQuestions(subscribe);
        Period period = Period.between(baseDate, today);

        return questions.get(period.getDays() % questions.size());
    }

    private void validateInvalidDate(LocalDate today) {
        if (baseDate.isAfter(today)) {
            throw new IllegalArgumentException("질문지를 결정할 수 없습니다.");
        }
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
