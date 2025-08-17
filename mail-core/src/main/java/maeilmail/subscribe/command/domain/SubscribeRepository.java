package maeilmail.subscribe.command.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SubscribeRepository extends JpaRepository<Subscribe, Long> {

    Optional<Subscribe> findByEmailAndTokenAndDeletedAtIsNull(String email, String token);

    List<Subscribe> findAllByEmailAndDeletedAtIsNull(String email);

    List<Subscribe> findAllByCreatedAtBeforeAndDeletedAtIsNullAndFrequency(LocalDateTime baseDateTime, SubscribeFrequency frequency);

    @Query(value = """
            update subscribe as s
            set s.next_question_sequence = s.next_question_sequence +
            (
                select count(*) as amount
                from subscribe_question as sq
                where
                    sq.subscribe_id = s.id and
                    sq.created_at > curdate()
            )
            where
                s.deleted_at is null and
                s.created_at < :baseDateTime
            """, nativeQuery = true)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void increaseNextQuestionSequence(LocalDateTime baseDateTime);

    @Query(value = """
            update subscribe as s
            set s.next_question_sequence = s.next_question_sequence + :sendCount
            where
                s.deleted_at is null and
                s.created_at < :baseDateTime and
                exists (
                    select 1
                    from subscribe_question as sq
                    where
                        sq.created_at > curdate() and
                        sq.subscribe_id = s.id
                    group by sq.subscribe_id
                    having count(sq.id) = :sendCount
                )
            """, nativeQuery = true)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void increasePartialNextQuestionSequence(LocalDateTime baseDateTime, int sendCount);
}
