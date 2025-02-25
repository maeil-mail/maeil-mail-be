package maeilmail.statistics;

import static maeilmail.subscribe.command.domain.QSubscribe.subscribe;
import static maeilmail.subscribe.command.domain.QSubscribeQuestion.subscribeQuestion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통계성 쿼리를 관리한다.
 */
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
class StatisticsDao {

    private final JPAQueryFactory queryFactory;

    public Long countDistinctSubscribeCount() {
        return queryFactory.select(subscribe.email.countDistinct())
                .from(subscribe)
                .where(subscribe.deletedAt.isNull())
                .fetchOne();
    }

    // 주어진 일자의 중복을 제거한 구독자를 조회하는 용도로 사용되므로 논리 삭제는 고려하지 않는다.
    public Long countDistinctSubscribeOnSpecificDate(LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return queryFactory.select(subscribe.email.countDistinct())
                .from(subscribe)
                .where(subscribe.createdAt.between(startOfDay, endOfDay))
                .fetchOne();
    }

    public Map<Boolean, Long> querySuccessFailCount(LocalDateTime dateTime) {
        List<Tuple> fetch = queryFactory.select(subscribeQuestion.isSuccess, subscribeQuestion.count())
                .from(subscribeQuestion)
                .where(subscribeQuestion.createdAt.between(dateTime, dateTime.plusDays(1).minusSeconds(1)))
                .groupBy(subscribeQuestion.isSuccess)
                .fetch();

        return fetch.stream().collect(getSuccessFailMapCollector());
    }

    public Map<SubscribeFrequency, Long> querySubscribeCountForFrequency(LocalDateTime dateTime) {
        List<Tuple> fetch = queryFactory.select(subscribe.frequency, subscribe.count())
                .from(subscribe)
                .where(subscribe.createdAt.before(dateTime)
                        .and(subscribe.deletedAt.isNull().or(subscribe.deletedAt.after(dateTime))))
                .groupBy(subscribe.frequency)
                .fetch();

        return fetch.stream().collect(getSubscribbeCountMapCollector());
    }

    private Collector<Tuple, ?, Map<Boolean, Long>> getSuccessFailMapCollector() {
        return Collectors.toMap(tuple -> tuple.get(0, Boolean.class), tuple -> tuple.get(1, Long.class));
    }

    private Collector<Tuple, ?, Map<SubscribeFrequency, Long>> getSubscribbeCountMapCollector() {
        return Collectors.toMap(tuple -> tuple.get(0, SubscribeFrequency.class), tuple -> tuple.get(1, Long.class));
    }
}
