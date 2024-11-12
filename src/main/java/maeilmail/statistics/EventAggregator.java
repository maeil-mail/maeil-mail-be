package maeilmail.statistics;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import maeilmail.subscribequestion.SubscribeQuestion;
import org.springframework.stereotype.Component;

@Component
class EventAggregator {

    public EventReport aggregate(List<SubscribeQuestion> subscribeQuestions) {
        Map<Boolean, Long> result = subscribeQuestions.stream()
                .collect(Collectors.partitioningBy(SubscribeQuestion::isSuccess, Collectors.counting()));

        return new EventReport("subscribeQuestion", result.get(true), result.get(false));
    }
}
