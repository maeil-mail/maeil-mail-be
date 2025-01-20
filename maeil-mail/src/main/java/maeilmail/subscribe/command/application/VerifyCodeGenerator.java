package maeilmail.subscribe.command.application;

import java.security.SecureRandom;
import java.util.Random;
import org.springframework.stereotype.Component;

@Component
class VerifyCodeGenerator {

    private static final int CODE_LENGTH = 4;
    private static final int RAND_BOUND = 10;
    private static final Random SECURE_RANDOM = new SecureRandom();

    public String generateCode() {
        StringBuilder code = new StringBuilder();

        for (int eachIndex = 0; eachIndex < CODE_LENGTH; eachIndex++) {
            code.append(pickOne());
        }

        return code.toString();
    }

    private String pickOne() {
        int eachValue = SECURE_RANDOM.nextInt(RAND_BOUND);

        return Integer.toString(eachValue);
    }
}
