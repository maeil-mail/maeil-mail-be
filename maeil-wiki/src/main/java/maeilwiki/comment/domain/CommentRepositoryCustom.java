package maeilwiki.comment.domain;

import maeilwiki.comment.dto.CommentSummary;

import java.util.List;

public interface CommentRepositoryCustom {

    List<CommentSummary> queryByWikiIdAndIdGreaterThan(Long wikiId, Long memberId, Long cursorId, Long size);

    Long countAllByWikiId(Long wikiId);
}
