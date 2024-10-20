package maeilmail.subscribe.core.policy;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionQueryService;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.core.ChoiceQuestionPolicy;
import maeilmail.subscribe.core.Subscribe;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@RequiredArgsConstructor
class PersonalSequenceChoicePolicy implements ChoiceQuestionPolicy {

    private final LocalDate backendDefaultDate = LocalDate.of(2024, 10, 16);
    private final LocalDate frontendDefaultDate = LocalDate.of(2024, 10, 14);
    private final QuestionQueryService questionQueryService;

    @Override
    public QuestionSummary choice(Subscribe subscribe, LocalDate today) {
        LocalDate subscribeDate = getOrDefaultSubscribeDate(subscribe.getSubscribeDate(), subscribe.getCategory());
        validateInvalidDate(subscribeDate, today);
        List<QuestionSummary> questions = findQuestions(subscribe);
        Period period = Period.between(subscribeDate, today);

        return questions.get(period.getDays() % questions.size());
    }

    private LocalDate getOrDefaultSubscribeDate(LocalDate subscribeDate, QuestionCategory category) {
        if (subscribeDate == null) {
            return getDefaultSubscribeDate(category);
        }

        return subscribeDate;
    }

    private LocalDate getDefaultSubscribeDate(QuestionCategory category) {
        if (category.equals(QuestionCategory.BACKEND)) {
            return backendDefaultDate;
        }

        return frontendDefaultDate;
    }

    private void validateInvalidDate(LocalDate subscribeDate, LocalDate today) {
        if (subscribeDate.isAfter(today)) {
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
