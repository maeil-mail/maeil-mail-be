package maeilbatch.support;

import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import maeilmail.mail.MailSender;
import maeilmail.subscribe.command.application.VerifySubscribeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(IntegrationTestSupport.TestConfig.class)
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
        public MailSender emailSender() {
            MailSender mailSender = mock(MailSender.class);
            willDoNothing()
                    .given(mailSender)
                    .sendMail(any());

            return mailSender;
        }

        @Bean
        public VerifySubscribeService verifySubscribeService() {
            VerifySubscribeService verifySubscribeService = mock(VerifySubscribeService.class);
            willDoNothing()
                    .given(verifySubscribeService)
                    .verify(any(), any());

            return verifySubscribeService;
        }

        @Bean(name = "mailBatchIntegrationTestJpaQueryFactory")
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
