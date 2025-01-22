package maeilwiki.wiki.infrastructure;

import static maeilwiki.member.QMember.member;
import static maeilwiki.wiki.QWiki.wiki;

import java.util.Optional;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maeilwiki.member.dto.QMemberThumbnail;
import maeilwiki.wiki.domain.WikiRepositoryCustom;
import maeilwiki.wiki.dto.QWikiSummary;
import maeilwiki.wiki.dto.WikiSummary;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
class WikiRepositoryImpl implements WikiRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<WikiSummary> queryOneById(Long wikiId) {
        WikiSummary wikiSummary = queryFactory.select(projectionWikiSummary())
                .from(wiki)
                .innerJoin(wiki.member, member)
                .where(wiki.id.eq(wikiId).and(wiki.deletedAt.isNull()))
                .fetchOne();
        return Optional.ofNullable(wikiSummary);
    }

    private QWikiSummary projectionWikiSummary() {
        return new QWikiSummary(
                wiki.id,
                wiki.question,
                wiki.questionDetail,
                wiki.category.stringValue().lower(),
                projectionMemberThumbnail(),
                wiki.createdAt
        );
    }

    private QMemberThumbnail projectionMemberThumbnail() {
        return new QMemberThumbnail(member.name, member.profileImageUrl, member.githubUrl);
    }
}
