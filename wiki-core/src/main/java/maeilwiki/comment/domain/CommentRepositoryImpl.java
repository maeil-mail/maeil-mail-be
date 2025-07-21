package maeilwiki.comment.domain;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;
import static maeilwiki.comment.domain.QComment.comment;
import static maeilwiki.comment.domain.QCommentLike.commentLike;
import static maeilwiki.member.domain.QMember.member;

import java.util.List;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maeilwiki.comment.dto.CommentSummary;
import maeilwiki.comment.dto.QCommentSummary;
import maeilwiki.member.dto.QMemberThumbnail;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CommentSummary> queryAllByWikiId(Long wikiId) {
        return queryFactory.from(comment)
                .join(member).on(comment.member.eq(member))
                .leftJoin(commentLike).on(comment.eq(commentLike.comment))
                .where(comment.wikiId.eq(wikiId)
                        .and(comment.deletedAt.isNull()))
                .orderBy(comment.id.asc())
                .transform(groupBy(comment.id)
                        .list(projectionCommentSummary()));
    }

    private QCommentSummary projectionCommentSummary() {
        return new QCommentSummary(
                comment.id,
                comment.answer,
                comment.isAnonymous,
                comment.createdAt,
                set(commentLike.member.id),
                new QMemberThumbnail(member.id, member.name, member.profileImageUrl, member.githubUrl)
        );
    }
}
