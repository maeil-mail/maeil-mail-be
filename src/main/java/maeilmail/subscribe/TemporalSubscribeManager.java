package maeilmail.subscribe;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class TemporalSubscribeManager {

    private static final String INVALID_EMAIL_MESSAGE = "인증되지 않은 이메일입니다.";

    private final TemporalSubscribeRepository temporalSubscribeRepository;

    public void add(String email, String verifyCode) {
        temporalSubscribeRepository.findByEmail(email).ifPresent(temporalSubscribeRepository::delete);
        TemporalSubscribe temporalSubscribe = new TemporalSubscribe(email, verifyCode);
        temporalSubscribeRepository.save(temporalSubscribe);
    }

    public void verify(String email, String verifyCode) {
        TemporalSubscribe temporalSubscribe = temporalSubscribeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_EMAIL_MESSAGE));

        try {
            temporalSubscribe.verify(verifyCode);
        } catch (Exception e) {
            log.info("메일 인증 실패 email = {} message = {}", email, e.getMessage());
            throw new IllegalArgumentException(INVALID_EMAIL_MESSAGE, e);
        }
    }
}
