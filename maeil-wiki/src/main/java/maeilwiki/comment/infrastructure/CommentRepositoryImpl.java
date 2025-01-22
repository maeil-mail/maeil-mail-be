package maeilwiki.comment.infrastructure;

import static maeilwiki.comment.QComment.comment;
import static maeilwiki.member.QMember.member;

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
                .innerJoin(comment.member, member)
                .where(comment.wiki.id.eq(wikiId))
                .orderBy(comment.id.asc())
                .fetch();
    }

    private QCommentSummary projectionCommentSummary() {
        return new QCommentSummary(comment.id, comment.answer, projectionMemberThumbnail(), comment.createdAt);
    }

    private QMemberThumbnail projectionMemberThumbnail() {
        return new QMemberThumbnail(member.name, member.profileImageUrl, member.githubUrl);
    }
}
