package maeilmail.subscribe;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SubscribeRepository extends JpaRepository<Subscribe, Long> {

    @Query("""
            select distinct s.email
            from Subscribe s
            where s.deletedAt is null
            """)
    List<String> findDistinctEmails();

    /**
     * 주어진 일자의 중복을 제거한 구독자를 조회하는 용도로 사용되므로 논리 삭제는 고려하지 않는다.
     *
     * @see maeilmail.statistics.StatisticsService#countNewSubscribersOnSpecificDate(LocalDate)
     */
    @Query("""
            select distinct s.email
            from Subscribe s
            where s.createdAt between :startOfDay and :endOfDay
            """)
    List<String> findDistinctEmailsByCreatedAtBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

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
}
