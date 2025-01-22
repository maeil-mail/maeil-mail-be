package maeilwiki.wiki;

import maeilwiki.wiki.domain.WikiRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WikiRepository extends JpaRepository<Wiki, Long>, WikiRepositoryCustom {
}
