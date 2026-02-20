package maeilmail.subscribe.command.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.command.application.request.SubscribeRequest;
import maeilmail.subscribe.command.application.request.VerifyEmailRequest;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeCreatedEvent;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscribeService {

    private final SubscribeRepository subscribeRepository;
    private final VerifySubscribeService verifySubscribeService;
    private final ApplicationEventPublisher eventPublisher;


    @Transactional
    public void subscribe(SubscribeRequest request) {
        log.info("이메일 구독 요청, 이메일 = {}", request.email());
        trySubscribe(request);
        eventPublisher.publishEvent(new SubscribeCreatedEvent(request.email()));
        log.info("이메일 구독 성공, 이메일 = {}", request.email());
    }

    private void trySubscribe(SubscribeRequest request) {
        verifySubscribeService.verify(request.email(), request.code());
        List<Subscribe> subscribes = subscribeRepository.findAllByEmailAndDeletedAtIsNull(request.email());
        List<QuestionCategory> newSubscribeCategories = findNewSubscribeCategories(request, subscribes);
        if (newSubscribeCategories.isEmpty()) {
            return;
        }

        newSubscribeCategories.forEach(it -> subscribe(it, request));
        synchronizeFrequency(request, subscribes);
    }

    private List<QuestionCategory> findNewSubscribeCategories(SubscribeRequest request, List<Subscribe> subscribes) {
        return request.category().stream()
                .map(QuestionCategory::from)
                .filter(it -> isNotSubscribed(it, subscribes))
                .toList();
    }

    private boolean isNotSubscribed(QuestionCategory category, List<Subscribe> subscribes) {
        return subscribes.stream()
                .noneMatch(it -> it.getCategory() == category);
    }

    private void subscribe(QuestionCategory category, SubscribeRequest request) {
        SubscribeFrequency frequency = SubscribeFrequency.from(request.frequency());
        Subscribe subscribe = new Subscribe(request.email(), category, frequency);
        subscribeRepository.save(subscribe);
    }

    private void synchronizeFrequency(SubscribeRequest request, List<Subscribe> subscribes) {
        SubscribeFrequency frequency = SubscribeFrequency.from(request.frequency());
        subscribes.forEach(it -> it.changeFrequency(frequency));
    }

    public void sendCodeIncludedMail(VerifyEmailRequest request) {
        verifySubscribeService.sendCodeIncludedMail(request);
    }
}
