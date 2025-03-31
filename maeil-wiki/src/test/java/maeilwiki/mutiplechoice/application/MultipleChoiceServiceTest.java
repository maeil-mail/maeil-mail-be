package maeilwiki.mutiplechoice.application;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import maeilsupport.PaginationResponse;
import maeilwiki.member.application.MemberIdentity;
import maeilwiki.member.domain.Member;
import maeilwiki.member.domain.MemberRepository;
import maeilwiki.member.dto.MemberThumbnail;
import maeilwiki.mutiplechoice.domain.Option;
import maeilwiki.mutiplechoice.domain.Workbook;
import maeilwiki.mutiplechoice.domain.WorkbookQuestion;
import maeilwiki.support.IntegrationTestSupport;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

class MultipleChoiceServiceTest extends IntegrationTestSupport {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MultipleChoiceService multipleChoiceService;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("객관식 문제집 ID로 단건 조회를 수행한다.")
    void getWorkbookById() {
        WorkbookRequest workbookRequest = createWorkbookRequest("BACKEND", 2);
        Member member = createMember();
        MemberIdentity identity = new MemberIdentity(member.getId(), member.getName(), member.getProfileImageUrl());
        WorkbookCreatedResponse workbookCreatedResponse = multipleChoiceService.create(identity, workbookRequest);

        WorkbookResponse response = multipleChoiceService.getWorkbookById(workbookCreatedResponse.id());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.workbookTitle()).isEqualTo("title");
            softly.assertThat(response.difficultyLevel()).isEqualTo(5);
            softly.assertThat(response.category()).isEqualTo("backend");
            softly.assertThat(response.workbookDetail()).isEqualTo("detail");
            softly.assertThat(response.createdAt()).isNotNull();
            softly.assertThat(response.timeLimit()).isEqualTo(50);
            softly.assertThat(response.questionCount()).isEqualTo(2);
            softly.assertThat(response.solvedCount()).isEqualTo(0);

            MemberThumbnail workbookOwner = response.owner();
            softly.assertThat(workbookOwner.id()).isEqualTo(member.getId());
            softly.assertThat(workbookOwner.name()).isEqualTo(member.getName());
            softly.assertThat(workbookOwner.profileImage()).isEqualTo(member.getProfileImageUrl());
            softly.assertThat(workbookOwner.github()).isEqualTo(member.getGithubUrl());
            softly.assertThat(workbookOwner.github()).isEqualTo(member.getGithubUrl());

            List<QuestionResponse> questions = response.questions();
            softly.assertThat(questions.get(0).id()).isEqualTo(1);
            softly.assertThat(questions.get(0).title()).isEqualTo("question1");
            softly.assertThat(questions.get(0).correctAnswerExplanation()).isEqualTo("explanation1");
            softly.assertThat(questions.get(1).id()).isEqualTo(2);
            softly.assertThat(questions.get(1).title()).isEqualTo("question2");
            softly.assertThat(questions.get(1).correctAnswerExplanation()).isEqualTo("explanation2");

            List<OptionResponse> options1 = questions.get(0).options();
            softly.assertThat(options1.get(0).id()).isEqualTo(1);
            softly.assertThat(options1.get(0).content()).isEqualTo("question1option1");
            softly.assertThat(options1.get(0).isCorrectAnswer()).isTrue();
            softly.assertThat(options1.get(1).id()).isEqualTo(2);
            softly.assertThat(options1.get(1).content()).isEqualTo("question1option2");
            softly.assertThat(options1.get(1).isCorrectAnswer()).isFalse();

            List<OptionResponse> options2 = questions.get(1).options();
            softly.assertThat(options2.get(0).id()).isEqualTo(3);
            softly.assertThat(options2.get(0).content()).isEqualTo("question2option1");
            softly.assertThat(options2.get(0).isCorrectAnswer()).isFalse();
            softly.assertThat(options2.get(1).id()).isEqualTo(4);
            softly.assertThat(options2.get(1).content()).isEqualTo("question2option2");
            softly.assertThat(options2.get(1).isCorrectAnswer()).isTrue();
        });
    }

    @Test
    @DisplayName("카테고리에 해당되는 문제집 페이지를 조회한다.")
    void pageByCategory() {
        WorkbookRequest workbookRequest1 = createWorkbookRequest("BACKEND", 1);
        WorkbookRequest workbookRequest2 = createWorkbookRequest("BACKEND", 2);
        WorkbookRequest workbookRequest3 = createWorkbookRequest("BACKEND", 3);
        WorkbookRequest workbookRequest4 = createWorkbookRequest("FRONTEND", 4);
        Member member = createMember();
        MemberIdentity identity = new MemberIdentity(member.getId(), member.getName(), member.getProfileImageUrl());
        List<Long> ids = List.of(workbookRequest1, workbookRequest2, workbookRequest3, workbookRequest4).stream()
                .map(req -> multipleChoiceService.create(identity, req).id())
                .toList();

        PaginationResponse<WorkbookResponse> workbookResponses = multipleChoiceService.pageByCategory("backend", PageRequest.of(0, 3));

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(workbookResponses.isLastPage()).isTrue();
            softAssertions.assertThat(workbookResponses.totalPage()).isEqualTo(1);
            softAssertions.assertThat(workbookResponses.data()).hasSize(3);
            softAssertions.assertThat(workbookResponses.data().get(0).id()).isEqualTo(ids.get(2));
            softAssertions.assertThat(workbookResponses.data().get(0).questionCount()).isEqualTo(3);
            softAssertions.assertThat(workbookResponses.data().get(1).id()).isEqualTo(ids.get(1));
            softAssertions.assertThat(workbookResponses.data().get(1).questionCount()).isEqualTo(2);
            softAssertions.assertThat(workbookResponses.data().get(2).id()).isEqualTo(ids.get(0));
            softAssertions.assertThat(workbookResponses.data().get(2).questionCount()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("객관식 문제를 출제한다.")
    void create() {
        WorkbookRequest workbookRequest = createWorkbookRequest("BACKEND", 2);
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

    private WorkbookRequest createWorkbookRequest(String category, int questionAndOptionSize) {
        List<QuestionRequest> questionRequests = new ArrayList<>();
        for (int i = 1; i <= questionAndOptionSize; i++) {
            List<OptionRequest> optionRequests = new ArrayList<>();

            for (int j = 1; j <= questionAndOptionSize; j++) {
                boolean isCorrectAnswer = i == j;
                OptionRequest optionRequest = new OptionRequest("question" + i + "option" + j, isCorrectAnswer);
                optionRequests.add(optionRequest);
            }

            QuestionRequest questionRequest = new QuestionRequest("question" + i, "explanation" + i, optionRequests);
            questionRequests.add(questionRequest);
        }

        return new WorkbookRequest("title", 5, category, "detail", 50, questionRequests);
    }

    private Member createMember() {
        Member member = new Member(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "GITHUB");
        member.setRefreshToken(UUID.randomUUID().toString());

        return memberRepository.save(member);
    }
}
