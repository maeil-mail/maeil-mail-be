package maeilmail.subscribe.command.application;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.subscribe.command.application.request.UnsubscribeRequest;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnsubscribeService {

    private final SubscribeRepository subscribeRepository;

    @Transactional
    public void unsubscribe(UnsubscribeRequest request) {
        Subscribe subscribe = subscribeRepository.findByEmailAndTokenAndDeletedAtIsNull(request.email(), request.token())
                .orElseThrow(NoSuchElementException::new);

        log.info("구독 해지 요청, 이메일 = {} 구독 분야 = {}", subscribe.getEmail(), subscribe.getCategory().name());
        subscribe.unsubscribe();
    }
}
