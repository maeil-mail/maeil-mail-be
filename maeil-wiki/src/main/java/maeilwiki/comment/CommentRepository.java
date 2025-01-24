package maeilwiki.comment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    boolean existsByWikiIdAndDeletedAtIsNull(Long wikiId);
}
