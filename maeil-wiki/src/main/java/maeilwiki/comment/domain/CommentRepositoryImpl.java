package maeilwiki.comment.domain;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maeilwiki.comment.dto.CommentSummary;
import maeilwiki.comment.dto.QCommentSummary;
import maeilwiki.member.dto.QMemberThumbnail;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static maeilwiki.comment.domain.QComment.comment;
import static maeilwiki.comment.domain.QCommentLike.commentLike;
import static maeilwiki.member.domain.QMember.member;

@RequiredArgsConstructor
@Transactional(readOnly = true)
class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CommentSummary> queryByWikiIdAndIdGreaterThan(Long wikiId, Long memberId, Long cursorId, Long size) {
        return queryFactory.select(projectionCommentSummary(memberId))
                .from(comment)
                .join(member).on(comment.member.eq(member))
                .where(comment.wikiId.eq(wikiId),
                        comment.deletedAt.isNull(),
                        idGreaterThan(cursorId))
                .orderBy(comment.id.asc())
                .limit(size)
                .fetch();
    }

    private QCommentSummary projectionCommentSummary(Long memberId) {
        return new QCommentSummary(
                comment.id,
                comment.answer,
                comment.isAnonymous,
                memberId != null
                        ? JPAExpressions.selectOne()
                        .from(commentLike)
                        .where(
                                commentLike.comment.eq(comment),
                                commentLike.member.id.eq(memberId)
                        )
                        .exists()
                        : Expressions.constant(false),
                JPAExpressions.select(commentLike.id.count())
                        .from(commentLike)
                        .where(commentLike.comment.eq(comment)),
                comment.createdAt,
                new QMemberThumbnail(member.id, member.name, member.profileImageUrl, member.githubUrl)
        );
    }

    private BooleanExpression idGreaterThan(Long cursorId) {
        if (cursorId == null) {
            return null;
        }
        return comment.id.gt(cursorId);
    }

    @Override
    public Long countAllByWikiId(Long wikiId) {
        return queryFactory.select(comment.id.count())
                .from(comment)
                .where(comment.wikiId.eq(wikiId))
                .fetchOne();
    }
}
