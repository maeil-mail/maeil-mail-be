package maeilmail.subscribe.core;

import maeilmail.question.QuestionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscribeRepository extends JpaRepository<Subscribe, Long> {

    boolean existsByEmailAndCategory(String email, QuestionCategory category);
}
