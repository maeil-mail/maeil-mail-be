package maeilbatch.forward;

import java.time.LocalDateTime;
import java.util.Map;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ForwardReader {

    private final EntityManagerFactory entityManagerFactory;

    public JpaCursorItemReader<ForwardLog> generate(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return new JpaCursorItemReaderBuilder<ForwardLog>()
                .name("forwardLogReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("""
                           select f
                           from ForwardLog f
                           where
                               f.createdAt >= :startDateTime and f.createdAt < :endDateTime
                           order by f.id ASC
                        """)
                .parameterValues(Map.of("startDateTime", startDateTime, "endDateTime", endDateTime))
                .hintValues(Map.of("org.hibernate.fetchSize", 100))
                .build();
    }
}
