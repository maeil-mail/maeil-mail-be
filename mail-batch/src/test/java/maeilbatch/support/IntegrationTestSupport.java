package maeilbatch.support;

import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import maeilmail.mail.MailViewRenderer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;

@Transactional
@SpringBatchTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = "classpath:bucket4j.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public abstract class IntegrationTestSupport {

    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
    }

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private AuditingHandler auditingHandler;

    @MockitoBean
    protected DateTimeProvider dateTimeProvider;

    @MockitoBean
    protected Clock clock;

    @MockitoBean
    protected JavaMailSender javaMailSender;

    @MockitoBean
    protected MailViewRenderer mailViewRenderer;

    @BeforeEach
    void setUp() {
        setAuditingTime(LocalDateTime.now());
        auditingHandler.setDateTimeProvider(dateTimeProvider);
    }

    @AfterEach
    void tearDown() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }

    protected void setAuditingTime(LocalDateTime time) {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        when(dateTimeProvider.getNow())
                .thenReturn(Optional.of(time));
        when(clock.getZone())
                .thenReturn(zoneId);
        when(clock.instant())
                .thenReturn(time.atZone(zoneId).toInstant());
    }
}
