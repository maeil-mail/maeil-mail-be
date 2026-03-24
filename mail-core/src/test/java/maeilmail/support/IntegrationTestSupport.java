package maeilmail.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import java.time.LocalDateTime;
import java.util.List;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import maeilmail.mail.MailSender;
import maeilmail.mail.MimeMessageCustomizer;
import maeilmail.mail.SimpleMailMessage;
import maeilmail.subscribe.command.application.VerifySubscribeService;
import maeilmail.support.DistributedRateLimitSupport;
import org.hibernate.cfg.AvailableSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(IntegrationTestSupport.TestConfig.class)
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
    private TestAuditingSupport testAuditingSupport;

    @MockitoBean
    protected DateTimeProvider dateTimeProvider;

    @MockitoBean
    protected MailSender emailSender;

    @MockitoBean
    protected VerifySubscribeService verifySubscribeService;

    @MockitoBean
    protected JavaMailSender javaMailSender;

    @MockitoBean
    protected MimeMessageCustomizer mimeMessageCustomizer;

    @MockitoBean
    protected DistributedRateLimitSupport distributedRateLimitSupport;

    @BeforeEach
    void setUp() {
        setJpaAuditingTime(LocalDateTime.now());
        doNothing().when(emailSender)
                .sendMail(any(SimpleMailMessage.class));
        doNothing().when(verifySubscribeService)
                .verify(anyString(), anyString());
    }

    @AfterEach
    void tearDown() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }

    protected void setJpaAuditingTime(LocalDateTime time) {
        testAuditingSupport.setJpaAuditingTime(time);
    }

    @EnableCaching
    @EnableJpaAuditing
    @TestConfiguration
    public static class TestConfig {

        @Bean
        public QueryCountTester queryCountTester() {
            return new QueryCountTester();
        }

        @Bean
        public TestAuditingSupport testAuditingSupport(
                AuditingHandler auditingHandler,
                DateTimeProvider dateTimeProvider
        ) {
            return new TestAuditingSupport(auditingHandler, dateTimeProvider);
        }

        @Bean
        public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
            QueryCountInspector queryCountInspector = new QueryCountInspector(queryCountTester());

            return hibernateProperties ->
                    hibernateProperties.put(AvailableSettings.STATEMENT_INSPECTOR, queryCountInspector);
        }

        @Bean(name = "mailCoreIntegrationTestJpaQueryFactory")
        public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
            return new JPAQueryFactory(JPQLTemplates.DEFAULT, entityManager);
        }

        @Bean
        public CacheManager cacheManager() {
            List<Cache> caches = List.of(new ConcurrentMapCache("question"));
            SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
            simpleCacheManager.setCaches(caches);

            return simpleCacheManager;
        }
    }
}
