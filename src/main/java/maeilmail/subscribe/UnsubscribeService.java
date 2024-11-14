package maeilmail.subscribe;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
class UnsubscribeService {

    private final SubscribeRepository subscribeRepository;

    @Transactional
    public void unsubscribe(UnsubscribeRequest request) {
        Subscribe subscribe = subscribeRepository.findByEmailAndToken(request.email(), request.token())
                .orElseThrow(NoSuchElementException::new);

        log.info("구독 해지 요청, 이메일 = {} 구독 분야 = {}", subscribe.getEmail(), subscribe.getCategory().name());
        subscribe.unsubscribe();
    }
}
