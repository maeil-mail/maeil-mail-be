package maeilmail.subscribe.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.core.request.SubscribeQuestionRequest;
import maeilmail.subscribe.core.request.VerifyEmailRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscribeQuestionService {

    private final SubscribeRepository subscribeRepository;
    private final SubscribeVerifyService subscribeVerifyService;

    @Transactional
    public void subscribe(SubscribeQuestionRequest request) {
        log.info("이메일 구독 요청, 이메일 = {}", request.email());

        subscribeVerifyService.verify(request.email(), request.code());
        QuestionCategory category = QuestionCategory.from(request.category());
        Subscribe subscribe = new Subscribe(request.email(), category);

        log.info("이메일 구독 성공, 이메일 = {}", request.email());
        subscribeRepository.save(subscribe);
    }

    public void sendCodeIncludedMail(VerifyEmailRequest request) {
        subscribeVerifyService.sendCodeIncludedMail(request);
    }
}
