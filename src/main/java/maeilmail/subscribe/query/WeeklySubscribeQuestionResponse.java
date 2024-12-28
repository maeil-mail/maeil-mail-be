package maeilmail.subscribe.query;

import java.util.ArrayList;
import java.util.List;

public record WeeklySubscribeQuestionResponse(String weekLabel, List<WeeklySubscribeQuestionSummary> questions) {

    public static WeeklySubscribeQuestionResponse of(List<WeeklySubscribeQuestionSummary> result, Long month, Long week) {
        List<WeeklySubscribeQuestionSummary> copied = copyOf(result);
        String weekLabel = month + "월 " + week + "주차";

        return new WeeklySubscribeQuestionResponse(weekLabel, copied);
    }

    private static List<WeeklySubscribeQuestionSummary> copyOf(List<WeeklySubscribeQuestionSummary> result) {
        List<WeeklySubscribeQuestionSummary> copied = new ArrayList<>();
        int size = result.size();
        for (int index = 0; index < size; index++) {
            WeeklySubscribeQuestionSummary origin = result.get(index);
            copied.add(getCopy(origin, index));
        }

        return copied;
    }

    private static WeeklySubscribeQuestionSummary getCopy(WeeklySubscribeQuestionSummary origin, long index) {
        return new WeeklySubscribeQuestionSummary(origin.getId(), index + 1, origin.getTitle());
    }
}
