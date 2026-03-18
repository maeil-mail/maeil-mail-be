package maeilbatch.forward;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ForwardDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void changeStateWithNewTx(List<? extends ForwardLog> logs, ForwardStatus status) {
        changeState(logs, status);
    }

    @Transactional
    public void changeState(List<? extends ForwardLog> logs, ForwardStatus status) {
        if (logs.isEmpty()) {
            return;
        }

        logs.forEach(it -> it.setStatus(status));
        doChangeStatus(logs, status);
    }

    private void doChangeStatus(List<? extends ForwardLog> logs, ForwardStatus status) {
        String sql = "update forward_log as f set f.status = :status, f.updated_at = :now where f.id in (:ids)";
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("status", status.name())
                .addValue("ids", logs.stream().map(ForwardLog::getId).toList())
                .addValue("now", LocalDateTime.now(clock));

        jdbcTemplate.update(sql, param);
    }

    @Transactional(readOnly = true)
    public ForwardIdRange queryIdRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        String sql = """
                select coalesce(min(id), 0) as min_id, coalesce(max(id), 0) as max_id
                from forward_log
                where
                    created_at >= :startDateTime and
                    created_at < :endDateTime
                """;
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("startDateTime", startDateTime)
                .addValue("endDateTime", endDateTime);

        return jdbcTemplate.queryForObject(sql, param, getForwardIdRangeRowMapper());
    }

    private RowMapper<ForwardIdRange> getForwardIdRangeRowMapper() {
        return (rs, rowNum) -> new ForwardIdRange(
                rs.getLong("min_id"),
                rs.getLong("max_id")
        );
    }

    @Transactional
    public void batchInsert(List<ForwardLog> logs) {
        if (logs.isEmpty()) {
            return;
        }

        doBatchInsert(logs);
    }

    private void doBatchInsert(List<ForwardLog> logs) {
        String sql = """
                insert into forward_log (target, subject, message, status, created_at, updated_at)
                values (:target, :subject, :message, :status, :createdAt, :updatedAt)
                """;
        SqlParameterSource[] params = createParam(logs);

        jdbcTemplate.batchUpdate(sql, params);
    }

    private SqlParameterSource[] createParam(List<ForwardLog> logs) {
        LocalDateTime now = LocalDateTime.now(clock);

        return logs.stream()
                .map(log -> new MapSqlParameterSource()
                        .addValue("target", log.getTarget())
                        .addValue("subject", log.getSubject())
                        .addValue("message", log.getMessage())
                        .addValue("status", log.getStatus().name())
                        .addValue("createdAt", now)
                        .addValue("updatedAt", now)
                )
                .toArray(SqlParameterSource[]::new);
    }
}
