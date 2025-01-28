package maeilwiki.wiki.infrastructure;

import static maeilwiki.comment.domain.QComment.comment;
import static maeilwiki.member.domain.QMember.member;
import static maeilwiki.wiki.domain.QWiki.wiki;

import java.util.Optional;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maeilwiki.member.dto.QMemberThumbnail;
import maeilwiki.wiki.domain.WikiCategory;
import maeilwiki.wiki.domain.WikiRepositoryCustom;
import maeilwiki.wiki.dto.QWikiSummary;
import maeilwiki.wiki.dto.QWikiSummaryWithCommentCount;
import maeilwiki.wiki.dto.WikiSummary;
import maeilwiki.wiki.dto.WikiSummaryWithCommentCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
class WikiRepositoryImpl implements WikiRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<WikiSummary> queryOneById(Long wikiId) {
        WikiSummary wikiSummary = queryFactory.select(projectionWikiSummary())
                .from(wiki)
                .join(member).on(wiki.member.eq(member))
                .where(wiki.id.eq(wikiId)
                        .and(isNotDeletedWiki()))
                .fetchOne();
        return Optional.ofNullable(wikiSummary);
    }

    @Override
    public Page<WikiSummaryWithCommentCount> pageByCategory(String category, Pageable pageable) {
        JPAQuery<Long> countQuery = queryFactory.select(wiki.count())
                .from(wiki)
                .where(isNotDeletedWiki()
                        .and(eqCategory(category)));

        JPAQuery<WikiSummaryWithCommentCount> resultQuery = queryFactory.select(
                        new QWikiSummaryWithCommentCount(projectionWikiSummary(), comment.count())
                )
                .from(wiki)
                .join(member).on(wiki.member.eq(member))
                .leftJoin(comment).on(wiki.id.eq(comment.wikiId))
                .where(isNotDeletedWiki()
                        .and(eqCategory(category))
                        .and(comment.deletedAt.isNull()))
                .groupBy(wiki.id)
                .orderBy(wiki.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        return PageableExecutionUtils.getPage(resultQuery.fetch(), pageable, countQuery::fetchOne);
    }

    private BooleanExpression isNotDeletedWiki() {
        return wiki.deletedAt.isNull();
    }

    private BooleanExpression eqCategory(String category) {
        if (category == null || category.equalsIgnoreCase("all")) {
            return null;
        }

        return wiki.category.eq(WikiCategory.from(category));
    }

    private QWikiSummary projectionWikiSummary() {
        return new QWikiSummary(
                wiki.id,
                wiki.question,
                wiki.questionDetail,
                wiki.category.stringValue().lower(),
                wiki.isAnonymous,
                wiki.createdAt,
                projectionMemberThumbnail()
        );
    }

    private QMemberThumbnail projectionMemberThumbnail() {
        return new QMemberThumbnail(member.name, member.profileImageUrl, member.githubUrl);
    }
}
