package maeilmail.subscribe.core.policy;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import maeilmail.question.QuestionQueryService;
import maeilmail.question.QuestionSummary;
import maeilmail.subscribe.core.ChoiceQuestionPolicy;
import maeilmail.subscribe.core.Subscribe;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class RandomChoicePolicy implements ChoiceQuestionPolicy {

    private final QuestionQueryService questionQueryService;

    @Override
    public QuestionSummary choice(Subscribe subscribe, LocalDate today) {
        Random rand = new Random();
        List<QuestionSummary> questions = questionQueryService.queryAllByCategory(subscribe.getCategory().name());
        int index = rand.nextInt(questions.size());

        return questions.get(index);
    }
}
