package maeilmail.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
class AsyncConfig implements AsyncConfigurer {

    /**
     * corePoolSize보다 적은 스레드가 있거나 maxPoolSize보다 적은 스레드가 실행중이고,
     * queueCapacity에 의해 정의된 큐 사이즈가 가득찰 경우 스레드가 생성된다.
     */
    @Override
    @Bean("mailExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(3);
        threadPoolTaskExecutor.setThreadNamePrefix("mail -");
        threadPoolTaskExecutor.initialize();

        return threadPoolTaskExecutor;
    }
}
