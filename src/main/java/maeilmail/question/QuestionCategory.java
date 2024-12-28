package maeilmail.question;

import java.util.Arrays;
import java.util.NoSuchElementException;

public enum QuestionCategory {

    FRONTEND("FE"), BACKEND("BE");

    private final String description;

    QuestionCategory(String description) {
        this.description = description;
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
}
