package maeilbatch.forward;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import maeilbatch.support.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ForwardReaderTest extends IntegrationTestSupport {

    private static final String EMAIL_FORMAT = "test%s@test.com";
    private static final String SUBJECT_FORMAT = "subject-%s";
    private static final String MESSAGE_FORMAT = "message-%s";

    @Autowired
    private ForwardRepository forwardRepository;

    @Autowired
    private ForwardReader forwardReader;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDownForwardLogs() {
        forwardRepository.deleteAll();
    }

    @Test
    @DisplayName("페이지 경계를 넘어도 범위 내 데이터를 누락 없이 읽는다.")
    void readAllAcrossPages() throws Exception {
        LocalDateTime baseDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0));
        saveForwardLogs(205, baseDateTime.plusMinutes(1), ForwardStatus.PENDING);
        saveForwardLogs(3, baseDateTime.minusMinutes(1), ForwardStatus.PENDING);
        saveForwardLogs(2, baseDateTime.plusDays(1), ForwardStatus.PENDING);

        List<ForwardLog> result = readAll(baseDateTime, baseDateTime.plusDays(1));

        assertThat(result).hasSize(205);
    }

    @Test
    @DisplayName("id 오름차순으로 데이터를 읽는다.")
    void sortByIdAscending() throws Exception {
        LocalDateTime baseDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0));
        saveForwardLogs(130, baseDateTime.plusMinutes(1), ForwardStatus.PENDING);

        List<ForwardLog> result = readAll(baseDateTime, baseDateTime.plusDays(1));

        List<Long> ids = result.stream().map(ForwardLog::getId).toList();
        List<Long> sorted = ids.stream().sorted(Comparator.naturalOrder()).toList();

        assertAll(
                () -> assertThat(ids).doesNotContainNull(),
                () -> assertThat(ids).isEqualTo(sorted)
        );
    }

    @Test
    @DisplayName("페이지1 조회 후 앞쪽에 데이터가 삽입돼도 페이지2에서 페이지1 마지막 데이터를 중복 조회하지 않는다.")
    void noDuplicateWhenInsertAtFrontBetweenPages() throws Exception {
        LocalDateTime baseDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0));
        List<ForwardLog> initialLogs = saveForwardLogs(200, baseDateTime.plusMinutes(1), ForwardStatus.PENDING);
        List<Long> initialIds = initialLogs.stream()
                .map(ForwardLog::getId)
                .toList();

        JdbcPagingItemReader<ForwardLog> reader = openReader(baseDateTime, baseDateTime.plusDays(1), new ExecutionContext());
        try {
            List<ForwardLog> firstPage = read(reader, 100);
            Long lastIdOfFirstPage = firstPage.get(99).getId();

            insertFrontRow(baseDateTime.plusMinutes(1));
            List<ForwardLog> remaining = readAll(reader);

            List<Long> allReadIds = new ArrayList<>();
            firstPage.forEach(it -> allReadIds.add(it.getId()));
            remaining.forEach(it -> allReadIds.add(it.getId()));
            long distinctCount = allReadIds.stream().distinct().count();

            assertAll(
                    () -> assertThat(remaining)
                            .extracting(ForwardLog::getId)
                            .doesNotContain(lastIdOfFirstPage),
                    () -> assertThat(allReadIds).hasSize(initialIds.size()),
                    () -> assertThat(distinctCount).isEqualTo(initialIds.size()),
                    () -> assertThat(allReadIds).containsExactlyInAnyOrderElementsOf(initialIds)
            );
        } finally {
            reader.close();
        }
    }

    @Test
    @DisplayName("페이지1 조회 후 앞쪽 데이터가 삭제돼도 페이지2에서 원래 읽어야 할 레코드를 누락하지 않는다.")
    void noMissingWhenDeleteAtFrontBetweenPages() throws Exception {
        LocalDateTime baseDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0));
        List<ForwardLog> initialLogs = saveForwardLogs(200, baseDateTime.plusMinutes(1), ForwardStatus.PENDING);
        List<Long> initialIds = initialLogs.stream()
                .map(ForwardLog::getId)
                .toList();

        JdbcPagingItemReader<ForwardLog> reader = openReader(baseDateTime, baseDateTime.plusDays(1), new ExecutionContext());
        try {
            List<ForwardLog> firstPage = read(reader, 100);
            Long deleteTargetId = firstPage.get(0).getId();
            forwardRepository.deleteById(deleteTargetId);

            List<ForwardLog> remaining = readAll(reader);
            List<Long> allReadIds = new ArrayList<>();
            firstPage.forEach(it -> allReadIds.add(it.getId()));
            remaining.forEach(it -> allReadIds.add(it.getId()));
            long distinctCount = allReadIds.stream().distinct().count();

            assertAll(
                    () -> assertThat(allReadIds).hasSize(initialIds.size()),
                    () -> assertThat(distinctCount).isEqualTo(initialIds.size()),
                    () -> assertThat(allReadIds).containsExactlyInAnyOrderElementsOf(initialIds)
            );
        } finally {
            reader.close();
        }
    }

    @Test
    @DisplayName("주어진 날짜 조건에 해당되는 로그를 조회한다.")
    void rangeWhereClauseWorks() throws Exception {
        LocalDateTime baseDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0));
        saveForwardLogs(2, baseDateTime.minusSeconds(1), ForwardStatus.PENDING);
        List<ForwardLog> inRange = saveForwardLogs(3, baseDateTime, ForwardStatus.PENDING);
        saveForwardLogs(2, baseDateTime.plusDays(1), ForwardStatus.PENDING);

        List<ForwardLog> result = readAll(baseDateTime, baseDateTime.plusDays(1));
        Set<Long> expected = inRange.stream()
                .map(ForwardLog::getId)
                .collect(HashSet::new, Set::add, Set::addAll);
        Set<Long> actual = result.stream()
                .map(ForwardLog::getId)
                .collect(HashSet::new, Set::add, Set::addAll);

        assertThat(actual).isEqualTo(expected);
    }

    private List<ForwardLog> readAll(LocalDateTime startDateTime, LocalDateTime endDateTime) throws Exception {
        JdbcPagingItemReader<ForwardLog> reader = openReader(startDateTime, endDateTime, new ExecutionContext());
        try {
            return readAll(reader);
        } finally {
            reader.close();
        }
    }

    private JdbcPagingItemReader<ForwardLog> openReader(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            ExecutionContext executionContext
    ) throws Exception {
        JdbcPagingItemReader<ForwardLog> reader = forwardReader.generate(startDateTime, endDateTime);
        reader.afterPropertiesSet();
        reader.open(executionContext);

        return reader;
    }

    private List<ForwardLog> readAll(JdbcPagingItemReader<ForwardLog> reader) throws Exception {
        List<ForwardLog> result = new ArrayList<>();
        ForwardLog item;
        while ((item = reader.read()) != null) {
            result.add(item);
        }

        return result;
    }

    private List<ForwardLog> read(JdbcPagingItemReader<ForwardLog> reader, int count) throws Exception {
        List<ForwardLog> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ForwardLog item = reader.read();
            if (item == null) {
                break;
            }
            result.add(item);
        }

        return result;
    }

    private List<ForwardLog> saveForwardLogs(int size, LocalDateTime createdAt, ForwardStatus status) {
        setJpaAuditingTime(createdAt);
        List<ForwardLog> logs = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            logs.add(createForwardLog(i, status));
        }

        return forwardRepository.saveAll(logs);
    }

    private ForwardLog createForwardLog(int index, ForwardStatus status) {
        ForwardLog forwardLog = new ForwardLog(
                EMAIL_FORMAT.formatted(index),
                SUBJECT_FORMAT.formatted(index),
                MESSAGE_FORMAT.formatted(index)
        );
        forwardLog.setStatus(status);

        return forwardLog;
    }

    private void insertFrontRow(LocalDateTime createdAt) {
        jdbcTemplate.update(
                """
                        insert into forward_log(id, target, subject, message, status, created_at, updated_at)
                        values (?, ?, ?, ?, ?, ?, ?)
                        """,
                0L,
                "front@test.com",
                "front-subject",
                "front-message",
                ForwardStatus.PENDING.name(),
                createdAt,
                createdAt
        );
    }
}
