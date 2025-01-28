package maeilwiki.wiki;

import java.util.Optional;
import maeilwiki.wiki.domain.WikiRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WikiRepository extends JpaRepository<Wiki, Long>, WikiRepositoryCustom {

    Optional<Wiki> findByIdAndDeletedAtIsNull(Long id);
}
