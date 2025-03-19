package maeilwiki.wiki.domain;

import maeilwiki.wiki.dto.MultipleChoiceWikiSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface MultipleChoiceWikiRepositoryCustom {

    Optional<MultipleChoiceWikiSummary> queryOneById(Long id);

    Page<MultipleChoiceWikiSummary> pageByCategory(String category, Pageable pageable);
}
