package maeilmail.subscribe.command.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.subscribe.command.domain.TemporalSubscribe;
import maeilmail.subscribe.command.domain.TemporalSubscribeRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemporalSubscribeManager {

    private static final String INVALID_EMAIL_MESSAGE = "인증되지 않은 이메일입니다.";

    private final TemporalSubscribeRepository temporalSubscribeRepository;

    public void add(String email, String verifyCode) {
        List<TemporalSubscribe> temporalSubscribes = temporalSubscribeRepository.findAllByEmail(email);
        removeDataIfPresent(temporalSubscribes);

        TemporalSubscribe temporalSubscribe = new TemporalSubscribe(email, verifyCode);
        temporalSubscribeRepository.save(temporalSubscribe);
    }

    private void removeDataIfPresent(List<TemporalSubscribe> temporalSubscribes) {
        if (!temporalSubscribes.isEmpty()) {
            List<Long> ids = temporalSubscribes.stream()
                    .map(TemporalSubscribe::getId)
                    .toList();

            temporalSubscribeRepository.removeAllByIdIn(ids);
        }
    }

    public void verify(String email, String verifyCode) {
        List<TemporalSubscribe> temporalSubscribes = temporalSubscribeRepository.findAllByEmail(email);
        temporalSubscribes.stream()
                .filter(it -> isVerifiable(verifyCode, it))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(INVALID_EMAIL_MESSAGE));
    }

    private boolean isVerifiable(String verifyCode, TemporalSubscribe temporalSubscribe) {
        try {
            temporalSubscribe.verify(verifyCode);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
