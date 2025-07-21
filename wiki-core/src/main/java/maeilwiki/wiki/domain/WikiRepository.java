package maeilwiki.wiki.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WikiRepository extends JpaRepository<Wiki, Long>, WikiRepositoryCustom {

    Optional<Wiki> findByIdAndDeletedAtIsNull(Long id);
}
