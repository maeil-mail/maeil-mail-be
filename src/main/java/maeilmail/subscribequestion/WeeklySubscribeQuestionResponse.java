package maeilmail.subscribequestion;

import java.util.List;

public record WeeklySubscribeQuestionResponse(String weekLabel, List<WeeklySubscribeQuestionSummary> questions) {
}
