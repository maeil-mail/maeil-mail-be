package maeilmail.subscribe;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import maeilmail.question.Question;
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

    @Query("""
            update Subscribe s
            set s.nextQuestionSequence = s.nextQuestionSequence + 1
            where
                s.deletedAt is null and
                s.createdAt < :baseDateTime
            """)
    @Modifying
    void increaseNextQuestionSequence(LocalDateTime baseDateTime);
}
