package maeilwiki.comment.domain;

import java.util.List;
import maeilwiki.comment.dto.CommentSummary;

public interface CommentRepositoryCustom {

    List<CommentSummary> queryAllByWikiId(Long wikiId);
}
