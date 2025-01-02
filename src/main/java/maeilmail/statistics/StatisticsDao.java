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
                .where(subscribe.createdAt.before(dateTime).and(subscribe.deletedAt.isNull()))
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