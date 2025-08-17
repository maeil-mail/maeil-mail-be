package maeilbatch.mail;

import java.time.LocalDateTime;
import java.util.Map;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscribeItemReaderGenerator {

    private final EntityManagerFactory entityManagerFactory;

    public JpaCursorItemReader<Subscribe> generate(SubscribeFrequency frequency, LocalDateTime datetime) {
        String readerName = String.format("%sSubscribeReader", frequency.toLowerCase());

        return new JpaCursorItemReaderBuilder<Subscribe>()
                .name(readerName)
                .entityManagerFactory(entityManagerFactory)
                .queryString("""
                           select s
                           from Subscribe s
                           where
                               s.createdAt <= :createdAt and
                               s.deletedAt is null and
                               s.frequency = :frequency
                           order by s.id ASC
                        """)
                .parameterValues(Map.of(
                        "createdAt", datetime,
                        "frequency", frequency.toLowerCase()
                ))
                .hintValues(Map.of("org.hibernate.fetchSize", 100))
                .build();
    }
}
