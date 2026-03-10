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

/**
 * 오전 7시 이후 구독자는 다음날 전송된다.
 * 7시 이후 전송 과정 중에 빈도를 변경하거나, 구독해지한 사용자에 대해서는 조회 쿼리 발생 시점 데이터를 기준으로 삼는다.
 * 메일을 받기 이전에 설정한 값이므로, 허용하는 것이 사용자 입장에서 자연스럽다고 판단했다. (+ 7시 당시 빈도 스냅샷을 저장하는 비용이 있다.)
 */
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
