package maeilwiki.wiki.domain;

import java.util.Optional;
import maeilwiki.wiki.dto.WikiSummary;
import maeilwiki.wiki.dto.WikiSummaryWithCommentCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WikiRepositoryCustom {

    Optional<WikiSummary> queryOneById(Long wikiId);

    Page<WikiSummaryWithCommentCount> pageByCategory(String category, Pageable pageable);
}
