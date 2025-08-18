package maeilbatch.mail;

import java.time.LocalDateTime;
import java.util.Map;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import maeilmail.subscribe.command.domain.Subscribe;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailSendItemReader {

    private final EntityManagerFactory entityManagerFactory;

    public JpaCursorItemReader<Subscribe> generate(LocalDateTime datetime) {
        return new JpaCursorItemReaderBuilder<Subscribe>()
                .name("subscribeReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("""
                           select s
                           from Subscribe s
                           where
                               s.createdAt <= :createdAt and
                               s.deletedAt is null
                           order by s.id ASC
                        """)
                .parameterValues(Map.of("createdAt", datetime))
                .hintValues(Map.of("org.hibernate.fetchSize", 100))
                .build();
    }
}
