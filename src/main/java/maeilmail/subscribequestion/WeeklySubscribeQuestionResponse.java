package maeilmail.subscribequestion;

import java.util.List;

record WeeklySubscribeQuestionResponse(String weekLabel, List<WeeklySubscribeQuestionSummary> questions) {
}
