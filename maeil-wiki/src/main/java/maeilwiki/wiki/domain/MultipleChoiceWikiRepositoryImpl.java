package maeilwiki.wiki.domain;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maeilwiki.member.dto.QMemberThumbnail;
import maeilwiki.wiki.dto.MultipleChoiceWikiSummary;
import maeilwiki.wiki.dto.QMultipleChoiceWikiSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import static maeilwiki.comment.domain.QComment.comment;
import static maeilwiki.member.domain.QMember.member;
import static maeilwiki.wiki.domain.QMultipleChoiceWiki.multipleChoiceWiki;

@RequiredArgsConstructor
@Transactional(readOnly = true)
class MultipleChoiceWikiRepositoryImpl implements MultipleChoiceWikiRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<MultipleChoiceWikiSummary> queryOneById(Long wikiId) {
        MultipleChoiceWikiSummary wikiSummary = queryFactory.select(projectionMultipleChoiceWikiSummary())
                .from(multipleChoiceWiki)
                .join(member).on(multipleChoiceWiki.memberId.eq(member.id))
                .where(multipleChoiceWiki.id.eq(wikiId)
                        .and(isNotDeletedWiki()))
                .fetchOne();
        return Optional.ofNullable(wikiSummary);
    }

    @Override
    public Page<MultipleChoiceWikiSummary> pageByCategory(String category, Pageable pageable) {
        JPAQuery<Long> countQuery = queryFactory.select(multipleChoiceWiki.count())
                .from(multipleChoiceWiki)
                .where(isNotDeletedWiki()
                        .and(eqCategory(category)));

        JPAQuery<MultipleChoiceWikiSummary> resultQuery = queryFactory.select(projectionMultipleChoiceWikiSummary())
                .from(multipleChoiceWiki)
                .join(member).on(multipleChoiceWiki.memberId.eq(member.id))
                .leftJoin(comment).on(multipleChoiceWiki.id.eq(comment.wikiId))
                .where(isNotDeletedWiki().and(eqCategory(category)))
                .groupBy(multipleChoiceWiki.id)
                .orderBy(multipleChoiceWiki.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        return PageableExecutionUtils.getPage(resultQuery.fetch(), pageable, countQuery::fetchOne);
    }

    private QMultipleChoiceWikiSummary projectionMultipleChoiceWikiSummary() {
        return new QMultipleChoiceWikiSummary(
                multipleChoiceWiki.id,
                multipleChoiceWiki.title,
                multipleChoiceWiki.detail,
                multipleChoiceWiki.category.stringValue().lower(),
                multipleChoiceWiki.isAnonymous,
                multipleChoiceWiki.difficultyLevel,
                multipleChoiceWiki.multipleChoiceQuestions,
                multipleChoiceWiki.createdAt,
                projectionMemberThumbnail()
        );
    }

    private BooleanExpression isNotDeletedWiki() {
        return multipleChoiceWiki.deletedAt.isNull();
    }

    private BooleanExpression eqCategory(String category) {
        if (category == null || category.equalsIgnoreCase("all")) {
            return null;
        }

        return multipleChoiceWiki.category.eq(WikiCategory.from(category));
    }

    private QMemberThumbnail projectionMemberThumbnail() {
        return new QMemberThumbnail(member.id, member.name, member.profileImageUrl, member.githubUrl);
    }
}
