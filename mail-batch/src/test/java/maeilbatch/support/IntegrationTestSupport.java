package maeilbatch.support;

import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import maeilmail.mail.MailViewRenderer;
import maeilmail.subscribe.command.application.VerifySubscribeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBatchTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(IntegrationTestSupport.TestConfig.class)
@Sql(scripts = "classpath:bucket4j.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public abstract class IntegrationTestSupport {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private AuditingHandler auditingHandler;

    @Autowired
    private DateTimeProvider dateTimeProvider;

    @BeforeEach
    void setUp() {
        setJpaAuditingTime(LocalDateTime.now());
        auditingHandler.setDateTimeProvider(dateTimeProvider);
    }

    @AfterEach
    void tearDown() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }

    protected void setJpaAuditingTime(LocalDateTime time) {
        when(dateTimeProvider.getNow())
                .thenReturn(Optional.of(time));
    }

    @EnableCaching
    @EnableJpaAuditing
    @TestConfiguration
    public static class TestConfig {

        @Bean
        public DateTimeProvider dateTimeProvider() {
            return mock(DateTimeProvider.class);
        }

        @Bean
        public VerifySubscribeService verifySubscribeService() {
            VerifySubscribeService verifySubscribeService = mock(VerifySubscribeService.class);
            willDoNothing()
                    .given(verifySubscribeService)
                    .verify(any(), any());

            return verifySubscribeService;
        }

        @Bean
        public CacheManager cacheManager() {
            List<Cache> caches = List.of(new ConcurrentMapCache("question"));
            SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
            simpleCacheManager.setCaches(caches);

            return simpleCacheManager;
        }

        @Bean
        public JavaMailSender javaMailSender() {
            return mock(JavaMailSender.class);
        }

        @Bean
        public MailViewRenderer mailViewRenderer() {
            MailViewRenderer mailViewRenderer = mock(MailViewRenderer.class);
            when(mailViewRenderer.render(anyMap(), anyString()))
                    .thenReturn("mock-rendered-text");

            return mailViewRenderer;
        }
    }
}
