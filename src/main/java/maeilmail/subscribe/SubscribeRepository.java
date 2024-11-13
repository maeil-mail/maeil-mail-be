package maeilmail.subscribe;

import java.time.LocalDateTime;
import java.util.List;
import maeilmail.question.QuestionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SubscribeRepository extends JpaRepository<Subscribe, Long> {

    boolean existsByEmailAndCategory(String email, QuestionCategory category);

    @Query("select distinct s.email from Subscribe s")
    List<String> findDistinctEmails();

    @Query("select distinct s.email from Subscribe s where s.createdAt between :startOfDay and :endOfDay")
    List<String> findDistinctEmailsByCreatedAtBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

    List<Subscribe> findAllByCreatedAtBefore(LocalDateTime baseDateTime);

    @Query("""
            update Subscribe s
            set s.nextQuestionSequence = s.nextQuestionSequence + 1
            where s.createdAt < :baseDateTime
            """)
    @Modifying
    void increaseNextQuestionSequence(LocalDateTime baseDateTime);
}