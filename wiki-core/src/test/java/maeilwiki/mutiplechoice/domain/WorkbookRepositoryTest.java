package maeilwiki.mutiplechoice.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import maeilwiki.support.IntegrationTestSupport;
import maeilwiki.member.domain.Member;
import maeilwiki.member.domain.MemberRepository;
import maeilwiki.mutiplechoice.dto.OptionSummary;
import maeilwiki.mutiplechoice.dto.WorkbookQuestionSummary;
import maeilwiki.mutiplechoice.dto.WorkbookSummary;
import maeilwiki.mutiplechoice.dto.WorkbookSummaryWithQuestionCount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.support.TransactionTemplate;

class WorkbookRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private WorkbookRepository workbookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate txTemplate;

    @Test
    @DisplayName("문제집 아이디로 문제집을 단건 조회한다.")
    void queryOneById() {
        Member member = createMember();
        Workbook workbook = createWorkbook(member, "backend");

        WorkbookSummary workbookSummary = workbookRepository.queryOneById(workbook.getId()).orElseThrow();

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(workbookSummary.id()).isEqualTo(workbook.getId());
            softAssertions.assertThat(workbookSummary.workbookTitle()).isEqualTo(workbook.getTitle());
            softAssertions.assertThat(workbookSummary.workbookDetail()).isEqualTo(workbook.getWorkbookDetail());
            softAssertions.assertThat(workbookSummary.category()).isEqualTo(workbook.getCategory().toString().toLowerCase());
            softAssertions.assertThat(workbookSummary.solvedCount()).isEqualTo(workbook.getSolvedCount());
            softAssertions.assertThat(workbookSummary.timeLimit()).isEqualTo(workbook.getTimeLimit().getTimeLimit());
            softAssertions.assertThat(workbookSummary.difficultyLevel()).isEqualTo(workbook.getDifficultyLevel());
            softAssertions.assertThat(workbookSummary.owner().name()).isEqualTo(workbook.getMember().getName());
            softAssertions.assertThat(workbookSummary.owner().profileImage()).isEqualTo(workbook.getMember().getProfileImageUrl());
            softAssertions.assertThat(workbookSummary.owner().github()).isEqualTo(workbook.getMember().getGithubUrl());
        });
    }

    @Test
    @DisplayName("존재하지 않은 문제집 아이디로 조회하면 빈 Optional을 반환한다.")
    void queryOneByIdNotFound() {
        Long notFoundWorkbookId = 9999L;

        Optional<WorkbookSummary> wikiSummary = workbookRepository.queryOneById(notFoundWorkbookId);

        assertThat(wikiSummary).isEmpty();
    }

    @Test
    @DisplayName("문제집 아이디로 질문 목록을 조회한다.")
    void queryQuestionsByWorkbookId() {
        Member member = createMember();
        Workbook workbook = createWorkbook(member, "backend");
        WorkbookQuestion question1 = createQuestion(workbook);
        WorkbookQuestion question2 = createQuestion(workbook);

        List<WorkbookQuestionSummary> workbookQuestionSummaries = workbookRepository.queryQuestionsByWorkbookId(workbook.getId());

        assertThat(workbookQuestionSummaries)
                .map(WorkbookQuestionSummary::id)
                .containsExactlyElementsOf(List.of(question1.getId(), question2.getId()));
    }

    @Test
    @DisplayName("문제집 아이디에 해당하는 질문이 없으면 빈 컬렉션을 반환한다.")
    void queryQuestionsByWorkbookIdEmpty() {
        Long notFoundWorkbookId = 9999L;

        List<WorkbookQuestionSummary> workbookQuestionSummaries = workbookRepository.queryQuestionsByWorkbookId(notFoundWorkbookId);

        assertThat(workbookQuestionSummaries).isEmpty();
    }

    @Test
    @DisplayName("질문지 아이디 목록으로 옵션 목록을 조회한다.")
    void queryOptionsByQuestionIdsIn() {
        Member member = createMember();
        Workbook workbook = createWorkbook(member, "backend");
        WorkbookQuestion question1 = createQuestion(workbook);
        WorkbookQuestion question2 = createQuestion(workbook);
        Option option1 = createOption(question1);
        Option option2 = createOption(question1);
        Option option3 = createOption(question2);
        Option option4 = createOption(question2);
        List<Long> ids = List.of(question1.getId(), question2.getId());

        List<OptionSummary> optionSummaries = workbookRepository.queryOptionsByQuestionIdsIn(ids);

        assertThat(optionSummaries)
                .map(OptionSummary::id)
                .containsExactlyElementsOf(List.of(option1.getId(), option2.getId(), option3.getId(), option4.getId()));
    }

    @Test
    @DisplayName("질문지 아이디에 해당하는 항목이 없으면 빈 컬렉션을 반환한다.")
    void queryOptionsByQuestionIdsInEmpty() {
        List<Long> notFoundIds = List.of(9999L, 9998L);

        List<OptionSummary> optionSummaries = workbookRepository.queryOptionsByQuestionIdsIn(notFoundIds);

        assertThat(optionSummaries).isEmpty();
    }

    @Test
    @DisplayName("카테고리에 해당하는 문제집 페이지를 id 기준 내림차순으로 조회한다.")
    void pageByCategory() {
        Member member = createMember();
        Workbook backendWorkbook1 = createWorkbook(member, "backend");
        Workbook backendWorkbook2 = createWorkbook(member, "backend");
        Workbook backendWorkbook3 = createWorkbook(member, "backend");
        Workbook backendWorkbook4 = createWorkbook(member, "backend");
        Workbook frontendWorkbook1 = createWorkbook(member, "frontend");
        Workbook frontendWorkbook2 = createWorkbook(member, "frontend");
        createQuestions(backendWorkbook1, 1);
        createQuestions(backendWorkbook2, 2);
        createQuestions(backendWorkbook3, 3);
        createQuestions(backendWorkbook4, 4);
        Pageable pageable = PageRequest.of(0, 2);

        Page<WorkbookSummaryWithQuestionCount> workbookSummaryPage = workbookRepository.pageByCategory("backend", pageable);

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(workbookSummaryPage.getTotalElements()).isEqualTo(4);
            softAssertions.assertThat(workbookSummaryPage.getTotalPages()).isEqualTo(2);
            softAssertions.assertThat(workbookSummaryPage.getSize()).isEqualTo(2);
            softAssertions.assertThat(workbookSummaryPage.getContent().get(0).workbookSummary().id()).isEqualTo(backendWorkbook4.getId());
            softAssertions.assertThat(workbookSummaryPage.getContent().get(0).questionCount()).isEqualTo(4);
            softAssertions.assertThat(workbookSummaryPage.getContent().get(1).workbookSummary().id()).isEqualTo(backendWorkbook3.getId());
            softAssertions.assertThat(workbookSummaryPage.getContent().get(1).questionCount()).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("카테고리가 all 이면 모든 카테고리의 문제집 페이지를 조회한다.")
    void pageByDefaultCategory() {
        Member member = createMember();
        Workbook backendWorkbook1 = createWorkbook(member, "backend");
        Workbook backendWorkbook2 = createWorkbook(member, "backend");
        Workbook backendWorkbook3 = createWorkbook(member, "backend");
        Workbook backendWorkbook4 = createWorkbook(member, "backend");
        Workbook frontendWorkbook1 = createWorkbook(member, "frontend");
        Workbook frontendWorkbook2 = createWorkbook(member, "frontend");
        createQuestions(backendWorkbook1, 1);
        createQuestions(backendWorkbook2, 2);
        createQuestions(backendWorkbook3, 3);
        createQuestions(backendWorkbook4, 4);
        createQuestions(frontendWorkbook1, 1);
        createQuestions(frontendWorkbook2, 2);
        Pageable pageable = PageRequest.of(0, 3);

        Page<WorkbookSummaryWithQuestionCount> workbookSummaryPage = workbookRepository.pageByCategory("all", pageable);

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(workbookSummaryPage.getTotalElements()).isEqualTo(6);
            softAssertions.assertThat(workbookSummaryPage.getTotalPages()).isEqualTo(2);
            softAssertions.assertThat(workbookSummaryPage.getSize()).isEqualTo(3);
            softAssertions.assertThat(workbookSummaryPage.getContent().get(0).workbookSummary().id()).isEqualTo(frontendWorkbook2.getId());
            softAssertions.assertThat(workbookSummaryPage.getContent().get(0).questionCount()).isEqualTo(2);
            softAssertions.assertThat(workbookSummaryPage.getContent().get(1).workbookSummary().id()).isEqualTo(frontendWorkbook1.getId());
            softAssertions.assertThat(workbookSummaryPage.getContent().get(1).questionCount()).isEqualTo(1);
            softAssertions.assertThat(workbookSummaryPage.getContent().get(2).workbookSummary().id()).isEqualTo(backendWorkbook4.getId());
            softAssertions.assertThat(workbookSummaryPage.getContent().get(2).questionCount()).isEqualTo(4);
        });
    }

    private Member createMember() {
        Member member = new Member(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "GITHUB");
        member.setRefreshToken(UUID.randomUUID().toString());

        return memberRepository.save(member);
    }

    private Workbook createWorkbook(Member member, String category) {
        Workbook workbook = new Workbook("title", 4, category, "detail", 5, member);

        return workbookRepository.save(workbook);
    }

    private WorkbookQuestion createQuestion(Workbook workbook) {
        WorkbookQuestion workbookQuestion = new WorkbookQuestion("title", "explanation", workbook);

        return save(workbookQuestion);
    }

    private Option createOption(WorkbookQuestion workbookQuestion) {
        Option content = new Option("content", false, workbookQuestion);

        return save(content);
    }

    private void createQuestions(Workbook workbook, int times) {
        for (int i = 0; i < times; i++) {
            createQuestion(workbook);
        }
    }

    private <T> T save(T entity) {
        txTemplate.executeWithoutResult(it -> entityManager.persist(entity));

        return entity;
    }
}
