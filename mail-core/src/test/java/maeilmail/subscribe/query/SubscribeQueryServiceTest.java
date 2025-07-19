package maeilmail.subscribe.query;

import java.util.List;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeRepository;
import maeilmail.support.IntegrationTestSupport;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SubscribeQueryServiceTest extends IntegrationTestSupport {

    @Autowired
    private SubscribeQueryService subscribeQueryService;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Test
    @DisplayName("모든 구독자의 이메일을 중복 없이 조회한다.")
    void findAllWithUniqueEmail() {
        // given
        String duplicatedEmail = "prin@email.com";
        Subscribe subscribe1 = createSubscribe(duplicatedEmail);
        createSubscribe(duplicatedEmail);
        Subscribe subscribe3 = createSubscribe("atom@email.com");

        // when
        List<SubscribeEmail> subscribeEmails = subscribeQueryService.findAllWithUniqueEmail();

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(subscribeEmails).hasSize(2);
            softly.assertThat(subscribeEmails).extracting(SubscribeEmail::email)
                    .containsExactlyInAnyOrder(subscribe1.getEmail(), subscribe3.getEmail());
        });
    }

    private Subscribe createSubscribe(String email) {
        return subscribeRepository.save(new Subscribe(email, QuestionCategory.BACKEND, SubscribeFrequency.WEEKLY));
    }
}
