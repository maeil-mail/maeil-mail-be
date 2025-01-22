package maeilwiki.wiki.domain;

import java.util.Optional;
import maeilwiki.wiki.dto.WikiSummary;

public interface WikiRepositoryCustom {

    Optional<WikiSummary> queryOneById(Long wikiId);
}
