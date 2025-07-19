package maeilwiki.comment.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    boolean existsByWikiIdAndDeletedAtIsNull(Long wikiId);
}
