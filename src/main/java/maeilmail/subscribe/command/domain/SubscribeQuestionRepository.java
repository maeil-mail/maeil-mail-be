package maeilmail.subscribe.command.domain;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscribeQuestionRepository extends JpaRepository<SubscribeQuestion, Long> {

    List<SubscribeQuestion> findSubscribeQuestionByCreatedAtBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);
}
