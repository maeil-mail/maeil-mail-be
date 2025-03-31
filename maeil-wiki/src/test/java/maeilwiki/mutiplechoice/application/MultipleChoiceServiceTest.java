package maeilwiki.mutiplechoice.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import maeilwiki.member.application.MemberIdentity;
import maeilwiki.member.domain.Member;
import maeilwiki.member.domain.MemberRepository;
import maeilwiki.mutiplechoice.domain.Option;
import maeilwiki.mutiplechoice.domain.Workbook;
import maeilwiki.mutiplechoice.domain.WorkbookQuestion;
import maeilwiki.support.IntegrationTestSupport;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MultipleChoiceServiceTest extends IntegrationTestSupport {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MultipleChoiceService multipleChoiceService;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("객관식 문제를 출제한다.")
    void create() {
        WorkbookRequest workbookRequest = createWorkbookRequest();
        Member member = createMember();
        MemberIdentity identity = new MemberIdentity(member.getId(), member.getName(), member.getProfileImageUrl());

        multipleChoiceService.create(identity, workbookRequest);

        Workbook workbook = entityManager.createQuery("select wb from Workbook wb", Workbook.class).getSingleResult();
        List<WorkbookQuestion> questions = entityManager.createQuery("select q from WorkbookQuestion q", WorkbookQuestion.class).getResultList();
        List<Option> options = entityManager.createQuery("select o from Option o", Option.class).getResultList();
        List<Option> question1options = filterOption(options, questions, 0);
        List<Option> question2options = filterOption(options, questions, 1);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(questions).hasSize(2);
            softly.assertThat(questions)
                    .map(it -> it.getWorkbook().getId())
                    .containsOnly(workbook.getId());

            softly.assertThat(question1options)
                    .map(Option::getContent)
                    .containsExactlyElementsOf(List.of("question1option1", "question1option2"));
            softly.assertThat(question1options)
                    .map(Option::isCorrectAnswer)
                    .containsExactlyElementsOf(List.of(true, false));

            softly.assertThat(question2options)
                    .map(Option::getContent)
                    .containsExactlyElementsOf(List.of("question2option1", "question2option2"));
            softly.assertThat(question2options)
                    .map(Option::isCorrectAnswer)
                    .containsExactlyElementsOf(List.of(false, true));
        });
    }

    private List<Option> filterOption(List<Option> options, List<WorkbookQuestion> questions, int i) {
        return options.stream()
                .filter(it -> it.getQuestion().getId().equals(questions.get(i).getId()))
                .toList();
    }

    /**
     * 질문 2, 질문 당 항목 2, 정답은 같은 인덱스
     */
    private WorkbookRequest createWorkbookRequest() {
        List<QuestionRequest> questionRequests = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            List<OptionRequest> optionRequests = new ArrayList<>();

            for (int j = 1; j <= 2; j++) {
                boolean isCorrectAnswer = i == j;
                OptionRequest optionRequest = new OptionRequest("question" + i + "option" + j, isCorrectAnswer);
                optionRequests.add(optionRequest);
            }

            QuestionRequest questionRequest = new QuestionRequest("question" + i, "테스트", optionRequests);
            questionRequests.add(questionRequest);
        }

        return new WorkbookRequest("title", 5, "BACKEND", "detail", 50, questionRequests);
    }

    private Member createMember() {
        Member member = new Member(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "GITHUB");
        member.setRefreshToken(UUID.randomUUID().toString());

        return memberRepository.save(member);
    }
}
