package maeilwiki.wiki;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WikiRepository extends JpaRepository<Wiki, Long> {

    Optional<Wiki> findByIdAndDeletedAtIsNull(Long id);
}
