package maeilwiki.mutiplechoice.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import maeilwiki.member.domain.Member;
import maeilwiki.member.domain.MemberRepository;
import maeilwiki.mutiplechoice.dto.OptionSummary;
import maeilwiki.mutiplechoice.dto.WorkbookQuestionSummary;
import maeilwiki.mutiplechoice.dto.WorkbookSummary;
import maeilwiki.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
        Workbook workbook = createWorkbook(member);

        WorkbookSummary workbookSummary = workbookRepository.queryOneById(workbook.getId()).orElseThrow();

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(workbookSummary.id()).isEqualTo(workbook.getId());
            softAssertions.assertThat(workbookSummary.workbookTitle()).isEqualTo(workbook.getTitle());
            softAssertions.assertThat(workbookSummary.workbookDetail()).isEqualTo(workbook.getWorkbookDetail());
            softAssertions.assertThat(workbookSummary.category()).isEqualTo(workbook.getCategory().toString().toLowerCase());
            softAssertions.assertThat(workbookSummary.solvedCount()).isEqualTo(workbook.getSolvedCount());
            softAssertions.assertThat(workbookSummary.createdAt()).isEqualTo(workbook.getCreatedAt());
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
        Workbook workbook = createWorkbook(member);
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
        Workbook workbook = createWorkbook(member);
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

    private Member createMember() {
        Member member = new Member(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "GITHUB");
        member.setRefreshToken(UUID.randomUUID().toString());

        return memberRepository.save(member);
    }

    private Workbook createWorkbook(Member member) {
        Workbook workbook = new Workbook("title", 4, "backend", "detail", 5, member);

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

    private <T> T save(T entity) {
        txTemplate.executeWithoutResult(it -> entityManager.persist(entity));

        return entity;
    }
}
