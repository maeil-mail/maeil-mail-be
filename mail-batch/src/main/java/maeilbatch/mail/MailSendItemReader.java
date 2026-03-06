package maeilbatch.mail;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailSendItemReader {

    private final DataSource dataSource;

    public JdbcPagingItemReader<Subscribe> generate(LocalDateTime datetime) {
        return new JdbcPagingItemReaderBuilder<Subscribe>()
                .name("subscribeReader")
                .dataSource(dataSource)
                .pageSize(100)
                .selectClause("select id, email, category, next_question_sequence, token, deleted_at, frequency")
                .fromClause("from subscribe")
                .whereClause("where created_at <= :createdAt and deleted_at is null")
                .sortKeys(Map.of("id", Order.ASCENDING))
                .parameterValues(Map.of("createdAt", datetime))
                .rowMapper(getSubscribeRowMapper())
                .build();
    }

    private RowMapper<Subscribe> getSubscribeRowMapper() {
        return (rs, rowNum) -> new Subscribe(
                rs.getLong("id"),
                rs.getString("email"),
                QuestionCategory.from(rs.getString("category")),
                rs.getLong("next_question_sequence"),
                rs.getString("token"),
                rs.getTimestamp("deleted_at") == null
                        ? null
                        : rs.getTimestamp("deleted_at").toLocalDateTime(),
                SubscribeFrequency.from(rs.getString("frequency"))
        );
    }
}
