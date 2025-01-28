package maeilwiki.comment.infrastructure;

import static maeilwiki.comment.domain.QComment.comment;
import static maeilwiki.member.domain.QMember.member;

import java.util.List;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maeilwiki.comment.domain.CommentRepositoryCustom;
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
        return queryFactory.select(projectionCommentSummary())
                .from(comment)
                .join(member).on(comment.member.eq(member))
                .where(comment.wikiId.eq(wikiId)
                        .and(comment.deletedAt.isNull()))
                .orderBy(comment.id.asc())
                .fetch();
    }

    private QCommentSummary projectionCommentSummary() {
        return new QCommentSummary(
                comment.id,
                comment.answer,
                comment.isAnonymous,
                comment.createdAt,
                projectionMemberThumbnail()
        );
    }

    private QMemberThumbnail projectionMemberThumbnail() {
        return new QMemberThumbnail(member.name, member.profileImageUrl, member.githubUrl);
    }
}
