package maeilbatch.forward;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class StatusBatchChanger {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void changeState(List<? extends ForwardLog> logs, ForwardStatus status) {
        logs.forEach(it -> it.setStatus(status));
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("status", status.name().toLowerCase())
                .addValue("ids", logs.stream().map(ForwardLog::getId).toList())
                .addValue("now", LocalDateTime.now());

        jdbcTemplate.update("update forward_log as f set f.status = :status, f.updated_at = :now where f.id in (:ids)", param);
    }
}
