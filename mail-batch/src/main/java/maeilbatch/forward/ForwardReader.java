package maeilbatch.forward;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ForwardReader {

    private final DataSource dataSource;

    public JdbcPagingItemReader<ForwardLog> generate(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return generate(startDateTime, endDateTime, 1L, Long.MAX_VALUE);
    }

    public JdbcPagingItemReader<ForwardLog> generate(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Long startId,
            Long endId
    ) {
        return new JdbcPagingItemReaderBuilder<ForwardLog>()
                .name("forwardLogReader")
                .dataSource(dataSource)
                .pageSize(100)
                .selectClause("select id, target, subject, message, status")
                .fromClause("from forward_log")
                .whereClause("""
                        where created_at >= :startDateTime
                          and created_at < :endDateTime
                          and id between :startId and :endId
                        """)
                .sortKeys(Map.of("id", Order.ASCENDING))
                .parameterValues(Map.of(
                        "startDateTime", startDateTime,
                        "endDateTime", endDateTime,
                        "startId", startId,
                        "endId", endId
                ))
                .dataRowMapper(ForwardLog.class)
                .build();
    }
}
