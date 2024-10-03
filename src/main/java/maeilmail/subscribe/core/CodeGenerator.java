package maeilmail.subscribe.core;

import java.util.Random;
import org.springframework.stereotype.Component;

@Component
class CodeGenerator {

    private static final int CODE_LENGTH = 4;
    private static final int RAND_BOUND = 10;

    public String generateCode() {
        StringBuilder code = new StringBuilder();

        for (int eachIndex = 0; eachIndex < CODE_LENGTH; eachIndex++) {
            code.append(pickOne());
        }

        return code.toString();
    }

    private static String pickOne() {
        Random random = new Random();
        int eachValue = random.nextInt(RAND_BOUND);

        return Integer.toString(eachValue);
    }
}
