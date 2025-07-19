package maeilmail;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableCaching
@EnableJpaAuditing
@RequiredArgsConstructor
class CoreConfiguration {

    private final EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(JPQLTemplates.DEFAULT, entityManager);
    }

    @Bean
    public CacheManager cacheManager() {
        List<CaffeineCache> caffeineCaches = Arrays.stream(CacheType.values())
                .map(this::createCaffeineCache)
                .toList();
        SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(caffeineCaches);

        return simpleCacheManager;
    }

    private CaffeineCache createCaffeineCache(CacheType cacheType) {
        return new CaffeineCache(cacheType.getName(), Caffeine.newBuilder()
                .recordStats()
                .expireAfterWrite(cacheType.getExpiredAfterWrite(), TimeUnit.HOURS)
                .build()
        );
    }

    @Getter
    enum CacheType {

        QUESTION("question", 1L);

        private final String name;
        private final Long expiredAfterWrite;

        CacheType(String name, Long expiredAfterWrite) {
            this.name = name;
            this.expiredAfterWrite = expiredAfterWrite;
        }
    }
}

