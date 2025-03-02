package maeilmail.subscribe.command.application;

import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.subscribe.command.application.request.TransmissionFrequencyRequest;
import maeilmail.subscribe.command.application.response.TransmissionFrequencyResponse;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransmissionFrequencyService {

    private final SubscribeRepository subscribeRepository;

    @Transactional
    public void changeFrequency(TransmissionFrequencyRequest request) {
        log.info("질문 전송 주기 변경 요청, email = {}", request.email());
        List<Subscribe> subscribes = subscribeRepository.findAllByEmailAndDeletedAtIsNull(request.email());
        SubscribeFrequency frequency = SubscribeFrequency.from(request.frequency());
        validateExists(subscribes);
        validateRequestToken(subscribes, request.token());

        subscribes.forEach(it -> it.changeFrequency(frequency));
    }

    private void validateExists(List<Subscribe> subscribes) {
        if (subscribes.isEmpty()) {
            throw new NoSuchElementException();
        }
    }

    private void validateRequestToken(List<Subscribe> subscribes, String token) {
        subscribes.stream()
                .filter(it -> it.isMine(token))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("자신의 이메일 전송 주기만 변경 가능합니다."));
    }

    @Transactional(readOnly = true)
    public TransmissionFrequencyResponse getFrequency(String email) {
        List<Subscribe> subscribes = subscribeRepository.findAllByEmailAndDeletedAtIsNull(email);
        SubscribeFrequency frequency = subscribes.stream()
                .map(Subscribe::getFrequency)
                .findFirst()
                .orElseThrow(NoSuchElementException::new);

        return new TransmissionFrequencyResponse(frequency.toLowerCase());
    }
}
