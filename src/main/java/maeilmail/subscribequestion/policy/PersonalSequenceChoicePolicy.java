package maeilmail.subscribequestion.policy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilmail.question.QuestionCategory;
import maeilmail.question.QuestionQueryService;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.Subscribe;
import maeilmail.subscribequestion.ChoiceQuestionPolicy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class PersonalSequenceChoicePolicy implements ChoiceQuestionPolicy {

    private final LocalDateTime backendDefaultSubscribedAt = LocalDateTime.of(2024, 10, 11, 0, 0);
    private final LocalDateTime frontendDefaultSubscribedAt = LocalDateTime.of(2024, 10, 14, 0, 0);
    private final LocalTime datePlusBase = LocalTime.of(6, 59, 59);
    private final QuestionQueryService questionQueryService;

    @Override
    public QuestionSummary choice(Subscribe subscribe, LocalDate today) {
        LocalDateTime subscribeAt = getOrDefaultSubscribeAt(subscribe.getCreatedAt(), subscribe.getCategory());
        LocalDate actualDate = getActualDate(subscribeAt);
        validateInvalidDate(actualDate, today);
        List<QuestionSummary> questions = findQuestions(subscribe);
        Period period = Period.between(actualDate, today);

        return questions.get(period.getDays() % questions.size());
    }

    private LocalDate getActualDate(LocalDateTime subscribeAt) {
        LocalTime subscribedTime = subscribeAt.toLocalTime();
        if (subscribedTime.isAfter(datePlusBase)) {
            return subscribeAt.toLocalDate().plusDays(1);
        }

        return subscribeAt.toLocalDate();
    }

    private LocalDateTime getOrDefaultSubscribeAt(LocalDateTime subscribeAt, QuestionCategory category) {
        if (subscribeAt == null) {
            return getDefaultSubscribeDate(category);
        }

        return subscribeAt;
    }

    private LocalDateTime getDefaultSubscribeDate(QuestionCategory category) {
        if (category.equals(QuestionCategory.BACKEND)) {
            return backendDefaultSubscribedAt;
        }

        return frontendDefaultSubscribedAt;
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
