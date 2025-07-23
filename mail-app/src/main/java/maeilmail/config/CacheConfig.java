package maeilmail.config;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableCaching
@Configuration
class CacheConfig {

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
