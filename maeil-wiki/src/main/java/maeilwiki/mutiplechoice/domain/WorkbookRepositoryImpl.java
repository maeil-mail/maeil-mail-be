package maeilwiki.mutiplechoice.domain;

import static maeilwiki.member.domain.QMember.member;
import static maeilwiki.mutiplechoice.domain.QOption.option;
import static maeilwiki.mutiplechoice.domain.QWorkbook.workbook;
import static maeilwiki.mutiplechoice.domain.QWorkbookQuestion.workbookQuestion;

import java.util.List;
import java.util.Optional;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maeilwiki.member.dto.QMemberThumbnail;
import maeilwiki.mutiplechoice.dto.OptionSummary;
import maeilwiki.mutiplechoice.dto.QOptionSummary;
import maeilwiki.mutiplechoice.dto.QWorkbookQuestionSummary;
import maeilwiki.mutiplechoice.dto.QWorkbookSummary;
import maeilwiki.mutiplechoice.dto.WorkbookQuestionSummary;
import maeilwiki.mutiplechoice.dto.WorkbookSummary;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
class WorkbookRepositoryImpl implements WorkbookRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<WorkbookSummary> queryOneById(Long workbookId) {
        WorkbookSummary workbookSummary = queryFactory.select(projectionWorkbookSummary())
                .from(workbook)
                .join(member).on(workbook.member.eq(member))
                .where(workbook.id.eq(workbookId))
                .fetchOne();

        return Optional.ofNullable(workbookSummary);
    }

    @Override
    public List<WorkbookQuestionSummary> queryQuestionsByWorkbookId(Long workbookId) {
        return queryFactory.select(projectionWorkbookQuestionSummary())
                .from(workbookQuestion)
                .where(workbookQuestion.workbook.id.eq(workbookId))
                .fetch();
    }

    @Override
    public List<OptionSummary> queryOptionsByQuestionIdsIn(List<Long> questionIds) {
        return queryFactory.select(projectionOptionSummary())
                .from(option)
                .where(option.question.id.in(questionIds))
                .fetch();
    }

    private QWorkbookSummary projectionWorkbookSummary() {
        return new QWorkbookSummary(
                workbook.title,
                workbook.difficultyLevel,
                workbook.category.stringValue().lower(),
                workbook.workbookDetail,
                projectionMemberThumbnail(),
                workbook.createdAt,
                workbook.timeLimit.timeLimit,
                workbook.solvedCount
        );
    }

    private QMemberThumbnail projectionMemberThumbnail() {
        return new QMemberThumbnail(member.id, member.name, member.profileImageUrl, member.githubUrl);
    }

    private QWorkbookQuestionSummary projectionWorkbookQuestionSummary() {
        return new QWorkbookQuestionSummary(
                workbookQuestion.id,
                workbookQuestion.title,
                workbookQuestion.correctAnswerExplanation
        );
    }

    private QOptionSummary projectionOptionSummary() {
        return new QOptionSummary(
                option.id,
                option.question.id,
                option.content,
                option.isCorrectAnswer
        );
    }
}
