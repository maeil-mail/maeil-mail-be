package maeilbatch.mail.dao;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import maeilmail.subscribe.command.domain.SubscribeQuestion;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SubscribeQuestionDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<Long> findIdsByKeys(List<SubscribeQuestionKey> keys) {
        if (keys.isEmpty()) {
            return List.of();
        }

        return doFindIdsByKeys(keys);
    }

    private List<Long> doFindIdsByKeys(List<SubscribeQuestionKey> keys) {
        MapSqlParameterSource param = createFindIdsParam(keys);
        String placeholders = createTuplePlaceholders(keys.size());

        String sql = """
                select sq.id
                from subscribe_question sq
                where (sq.subscribe_id, sq.question_id) in (%s)
                """.formatted(placeholders);

        return jdbcTemplate.query(sql, param, (rs, rowNum) -> rs.getLong("id"));
    }

    private MapSqlParameterSource createFindIdsParam(List<SubscribeQuestionKey> keys) {
        MapSqlParameterSource param = new MapSqlParameterSource();
        for (int i = 0; i < keys.size(); i++) {
            SubscribeQuestionKey key = keys.get(i);
            param.addValue("subscribeId" + i, key.subscribeId());
            param.addValue("questionId" + i, key.questionId());
        }

        return param;
    }

    private String createTuplePlaceholders(int keySize) {
        return IntStream.range(0, keySize)
                .mapToObj(i -> "(:subscribeId%d, :questionId%d)".formatted(i, i))
                .collect(Collectors.joining(", "));
    }

    @Transactional
    public void batchInsert(List<SubscribeQuestion> subscribeQuestions) {
        if (subscribeQuestions.isEmpty()) {
            return;
        }

        doBatchInsert(subscribeQuestions);
    }

    private void doBatchInsert(List<SubscribeQuestion> subscribeQuestions) {
        String sql = """
                insert into subscribe_question (subscribe_id, question_id, is_success, created_at, updated_at)
                values (:subscribeId, :questionId, :isSuccess, :createdAt, :updatedAt)
                """;
        SqlParameterSource[] params = createBatchInsertParams(subscribeQuestions);

        jdbcTemplate.batchUpdate(sql, params);
    }

    private SqlParameterSource[] createBatchInsertParams(List<SubscribeQuestion> subscribeQuestions) {
        LocalDateTime now = LocalDateTime.now(clock);

        return subscribeQuestions.stream()
                .map(subscribeQuestion -> new MapSqlParameterSource()
                        .addValue("subscribeId", subscribeQuestion.getSubscribe().getId())
                        .addValue("questionId", subscribeQuestion.getQuestion().getId())
                        .addValue("isSuccess", subscribeQuestion.isSuccess())
                        .addValue("createdAt", now)
                        .addValue("updatedAt", now)
                )
                .toArray(SqlParameterSource[]::new);
    }

    @Transactional
    public void deleteByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return;
        }

        doDeleteByIds(ids);
    }

    private void doDeleteByIds(List<Long> ids) {
        String sql = "delete from subscribe_question where id in (:ids)";
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("ids", ids);

        jdbcTemplate.update(sql, param);
    }
}
