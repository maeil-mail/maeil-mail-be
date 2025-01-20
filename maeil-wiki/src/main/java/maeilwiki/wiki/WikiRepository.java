package maeilwiki.wiki;

import org.springframework.data.jpa.repository.JpaRepository;

interface WikiRepository extends JpaRepository<Wiki, Long> {
}
