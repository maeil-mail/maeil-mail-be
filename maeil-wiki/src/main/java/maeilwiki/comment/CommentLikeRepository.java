package maeilwiki.comment;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByCommentIdAndMemberId(Long commentId, Long memberId);
}
