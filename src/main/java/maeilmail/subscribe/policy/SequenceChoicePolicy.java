package maeilmail.subscribe.policy;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilmail.question.Question;
import maeilmail.question.QuestionRepository;
import maeilmail.subscribe.ChoiceQuestionPolicy;
import maeilmail.subscribe.Subscribe;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@RequiredArgsConstructor
class SequenceChoicePolicy implements ChoiceQuestionPolicy {

    private final LocalDate baseDate = LocalDate.of(2024, 9, 23);
    private final QuestionRepository questionRepository;

    @Override
    public Question choice(Subscribe subscribe, LocalDate today) {
        validateInvalidDate(today);
        List<Question> questions = findQuestions(subscribe);
        Period period = Period.between(baseDate, today);

        return questions.get(period.getDays() % questions.size());
    }

    private void validateInvalidDate(LocalDate today) {
        if (baseDate.isAfter(today)) {
            throw new IllegalArgumentException("질문지를 결정할 수 없습니다.");
        }
    }

    private List<Question> findQuestions(Subscribe subscribe) {
        List<Question> questions = questionRepository.findAllByCategoryOrderByIdAsc(subscribe.getCategory());
        validateQuestionEmpty(questions);

        return questions;
    }

    private void validateQuestionEmpty(List<Question> questions) {
        if (questions.isEmpty()) {
            throw new IllegalStateException("질문지를 결정할 수 없습니다.");
        }
    }
}
