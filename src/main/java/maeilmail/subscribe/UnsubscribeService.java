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

        subscribe.unsubscribe();
    }
}
