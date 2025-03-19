package maeilwiki.wiki.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MultipleChoiceWikiRepository extends JpaRepository<MultipleChoiceWiki, Long>, MultipleChoiceWikiRepositoryCustom {
}
