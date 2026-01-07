package maeilmail.question;

import java.util.Arrays;
import java.util.NoSuchElementException;

public enum QuestionCategory {

    FRONTEND("FE", 0L), BACKEND("BE", 15L);

    private final String description;
    private final Long initialSequence;

    QuestionCategory(String description, Long initialSequence) {
        this.description = description;
        this.initialSequence = initialSequence;
    }

    public static QuestionCategory from(String category) {
        return Arrays.stream(QuestionCategory.values())
                .filter((it) -> it.name().equalsIgnoreCase(category))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    public String toLowerCase() {
        return this.name().toLowerCase();
    }

    public String getDescription() {
        return description;
    }

    public Long getInitialSequence() {
        return initialSequence;
    }
}
