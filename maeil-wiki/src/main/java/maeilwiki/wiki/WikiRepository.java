package maeilwiki.wiki;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WikiRepository extends JpaRepository<Wiki, Long> {
}
