package maeilbatch.mail;

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
import java.util.UUID;
import maeilbatch.support.IntegrationTestSupport;
import maeilmail.question.QuestionCategory;
import maeilmail.subscribe.command.domain.Subscribe;
import maeilmail.subscribe.command.domain.SubscribeFrequency;
import maeilmail.subscribe.command.domain.SubscribeRepository;
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
class MailSendItemReaderTest extends IntegrationTestSupport {

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private MailSendItemReader mailSendItemReader;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDownSubscribes() {
        subscribeRepository.deleteAll();
    }

    @Test
    @DisplayName("페이지 경계를 넘어도 범위 내 데이터를 누락 없이 읽는다.")
    void readAllAcrossPages() throws Exception {
        LocalDateTime baseDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0));
        List<Subscribe> inRange = saveSubscribes(
                "in-range",
                205,
                baseDateTime.minusMinutes(1),
                SubscribeFrequency.DAILY,
                false
        );
        saveSubscribes("future", 3, baseDateTime.plusMinutes(1), SubscribeFrequency.DAILY, false);
        saveSubscribes("deleted", 2, baseDateTime.minusMinutes(1), SubscribeFrequency.DAILY, true);

        List<Subscribe> result = readAll(baseDateTime);
        Set<Long> actualIds = result.stream().map(Subscribe::getId).collect(HashSet::new, Set::add, Set::addAll);
        Set<Long> expectedIds = inRange.stream().map(Subscribe::getId).collect(HashSet::new, Set::add, Set::addAll);

        assertAll(
                () -> assertThat(result).hasSize(205),
                () -> assertThat(actualIds).isEqualTo(expectedIds)
        );
    }

    @Test
    @DisplayName("id 오름차순으로 구독자를 읽는다.")
    void sortByIdAscending() throws Exception {
        LocalDateTime baseDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0));
        saveSubscribes("sort", 130, baseDateTime.minusMinutes(1), SubscribeFrequency.WEEKLY, false);

        List<Subscribe> result = readAll(baseDateTime);
        List<Long> ids = result.stream().map(Subscribe::getId).toList();
        List<Long> sorted = ids.stream().sorted(Comparator.naturalOrder()).toList();

        assertAll(
                () -> assertThat(ids).doesNotContainNull(),
                () -> assertThat(ids).isEqualTo(sorted)
        );
    }

    @Test
    @DisplayName("탈퇴하지 않는 구독자만 조회한다.")
    void onlyReadNotDeleted() throws Exception {
        LocalDateTime baseDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0));
        List<Subscribe> active = saveSubscribes(
                "active",
                3,
                baseDateTime.minusMinutes(1),
                SubscribeFrequency.DAILY,
                false
        );
        saveSubscribes("deleted", 2, baseDateTime.minusMinutes(1), SubscribeFrequency.DAILY, true);

        List<Subscribe> result = readAll(baseDateTime);
        Set<Long> actualIds = result.stream().map(Subscribe::getId).collect(HashSet::new, Set::add, Set::addAll);
        Set<Long> expectedIds = active.stream().map(Subscribe::getId).collect(HashSet::new, Set::add, Set::addAll);

        assertThat(actualIds).isEqualTo(expectedIds);
    }

    @Test
    @DisplayName("주어진 날짜 이전(포함)에 생성된 구독자를 조회한다.")
    void createdAtConditionWorks() throws Exception {
        LocalDateTime baseDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0));
        List<Subscribe> expected = new ArrayList<>();
        expected.addAll(saveSubscribes("before", 2, baseDateTime.minusSeconds(1), SubscribeFrequency.DAILY, false));
        expected.addAll(saveSubscribes("equal", 3, baseDateTime, SubscribeFrequency.WEEKLY, false));
        saveSubscribes("after", 2, baseDateTime.plusSeconds(1), SubscribeFrequency.DAILY, false);

        List<Subscribe> result = readAll(baseDateTime);
        Set<Long> expectedIds = expected.stream().map(Subscribe::getId).collect(HashSet::new, Set::add, Set::addAll);
        Set<Long> actualIds = result.stream().map(Subscribe::getId).collect(HashSet::new, Set::add, Set::addAll);

        assertThat(actualIds).isEqualTo(expectedIds);
    }

    @Test
    @DisplayName("페이지1 조회 후 앞쪽에 데이터가 삽입돼도 페이지2에서 페이지1 마지막 데이터를 중복 조회하지 않는다.")
    void noDuplicateWhenInsertAtFrontBetweenPages() throws Exception {
        LocalDateTime baseDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0));
        List<Subscribe> initialSubs = saveSubscribes(
                "initial",
                200,
                baseDateTime.minusMinutes(1),
                SubscribeFrequency.DAILY,
                false
        );
        List<Long> initialIds = initialSubs.stream()
                .map(Subscribe::getId)
                .toList();

        JdbcPagingItemReader<Subscribe> reader = openReader(baseDateTime, new ExecutionContext());
        try {
            List<Subscribe> firstPage = read(reader, 100);
            Long lastIdOfFirstPage = firstPage.get(99).getId();
            Long firstIdOfFirstPage = firstPage.get(0).getId();

            insertFrontRow(firstIdOfFirstPage, baseDateTime.minusMinutes(1));
            List<Subscribe> remaining = readAll(reader);

            List<Long> allReadIds = new ArrayList<>();
            firstPage.forEach(it -> allReadIds.add(it.getId()));
            remaining.forEach(it -> allReadIds.add(it.getId()));
            long distinctCount = allReadIds.stream().distinct().count();

            assertAll(
                    () -> assertThat(remaining)
                            .extracting(Subscribe::getId)
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
        List<Subscribe> initialSubs = saveSubscribes(
                "initial",
                200,
                baseDateTime.minusMinutes(1),
                SubscribeFrequency.DAILY,
                false
        );
        List<Long> initialIds = initialSubs.stream().map(Subscribe::getId).toList();

        JdbcPagingItemReader<Subscribe> reader = openReader(baseDateTime, new ExecutionContext());
        try {
            List<Subscribe> firstPage = read(reader, 100);
            Long deleteTargetId = firstPage.get(0).getId();
            subscribeRepository.deleteById(deleteTargetId);

            List<Subscribe> remaining = readAll(reader);
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

    private List<Subscribe> readAll(LocalDateTime dateTime) throws Exception {
        JdbcPagingItemReader<Subscribe> reader = openReader(dateTime, new ExecutionContext());
        try {
            return readAll(reader);
        } finally {
            reader.close();
        }
    }

    private JdbcPagingItemReader<Subscribe> openReader(LocalDateTime dateTime, ExecutionContext executionContext)
            throws Exception {
        JdbcPagingItemReader<Subscribe> reader = mailSendItemReader.generate(dateTime);
        reader.afterPropertiesSet();
        reader.open(executionContext);

        return reader;
    }

    private List<Subscribe> readAll(JdbcPagingItemReader<Subscribe> reader) throws Exception {
        List<Subscribe> result = new ArrayList<>();
        Subscribe item;
        while ((item = reader.read()) != null) {
            result.add(item);
        }

        return result;
    }

    private List<Subscribe> read(JdbcPagingItemReader<Subscribe> reader, int count) throws Exception {
        List<Subscribe> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Subscribe item = reader.read();
            if (item == null) {
                break;
            }
            result.add(item);
        }

        return result;
    }

    private List<Subscribe> saveSubscribes(
            String emailPrefix,
            int size,
            LocalDateTime createdAt,
            SubscribeFrequency frequency,
            boolean deleted
    ) {
        setAuditingTime(createdAt);
        List<Subscribe> subscribes = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            subscribes.add(new Subscribe(
                    emailPrefix + "-" + i + "@test.com",
                    QuestionCategory.BACKEND,
                    frequency
            ));
        }

        List<Subscribe> saved = subscribeRepository.saveAll(subscribes);
        if (!deleted) {
            return saved;
        }

        saved.forEach(Subscribe::unsubscribe);
        return subscribeRepository.saveAll(saved);
    }

    private void insertFrontRow(Long id, LocalDateTime createdAt) {
        jdbcTemplate.update(
                """
                        insert into subscribe(
                            id, email, category, next_question_sequence, token, deleted_at, frequency, created_at, updated_at
                        )
                        values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                id - 1,
                "front@test.com",
                QuestionCategory.BACKEND.name(),
                15L,
                UUID.randomUUID().toString(),
                null,
                SubscribeFrequency.DAILY.name(),
                createdAt,
                createdAt
        );
    }
}
