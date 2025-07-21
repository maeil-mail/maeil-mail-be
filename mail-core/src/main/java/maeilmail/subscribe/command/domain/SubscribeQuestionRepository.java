package maeilmail.subscribe.command.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import maeilmail.question.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface SubscribeQuestionRepository extends JpaRepository<SubscribeQuestion, Long> {

    Optional<SubscribeQuestion> findBySubscribeAndQuestion(Subscribe subscribe, Question question);

    List<SubscribeQuestion> findBySubscribeAndQuestionIn(Subscribe subscribe, List<Question> question);

    @Query("""
            select sq
            from SubscribeQuestion sq
            join fetch sq.question q
            join fetch sq.subscribe s
            where
                sq.createdAt >= :baseDateTime and
                sq.isSuccess = false
            """)
    List<SubscribeQuestion> findAllFailedSubscribeQuestions(LocalDateTime baseDateTime);

    @Transactional
    void removeAllByIdIn(List<Long> ids);
}
